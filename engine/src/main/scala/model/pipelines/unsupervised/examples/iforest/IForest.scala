package model.pipelines.unsupervised.examples.iforest

import model.common.SharedParams

import scala.collection.mutable
import scala.util.Random
import org.apache.commons.math3.random.{RandomDataGenerator, RandomGeneratorFactory}
import org.apache.log4j.Logger
import org.apache.spark.ml.linalg._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.functions._

import scala.reflect.ClassTag


/**
 * Model of IF(isolation forest), including constructor, copy, write and get summary.
 * @param _trees Param of trees for constructor
 */
class IForestModel (val _trees: Array[IFNode]) extends Serializable {

    require(_trees.nonEmpty, "IForestModel requires at least 1 tree.")

    def trees: Array[IFNode] = _trees
}

/**
 * Isolation Forest (iForest) is a effective model that focuses on anomaly isolation.
 * iForest uses tree structure for modeling data, iTree isolates anomalies closer to
 * the root of the tree as compared to normal points.
 *
 * A anomaly score is calculated by iForest model to measure the abnormality of the
 * data instances. The higher, the more abnormal.
 *
 * More details about iForest can be found in paper
 * <a href="https://dl.acm.org/citation.cfm?id=1511387">Isolation Forest</a>
 *
 * iForest on Spark is trained via model-wise parallelism, and predicts a new Dataset via data-wise parallelism,
 * It is implemented in the following steps:
 * 1. Sampling data from a Dataset. Data instances are sampled and grouped for each iTree. As indicated in the paper,
 * the number samples for constructing each tree is usually not very large (default value 256). Thus we can construct
 * a sampled paired RDD, where each row key is tree index and row value is a group of sampled data instances for a tree.
 * 2. Training and constructing each iTree on parallel via a map operation and collect the iForest model in the driver.
 * 3. Predict a new Dataset on parallel via a map operation with the collected iForest model.
 *
 * Revised based on repo: <a href="https://github.com/titicaca/spark-iforest"> Github-IForest</a>
 */
class IForest (params: IsolationForestParams, featuresCol: String="featureVec") extends Serializable {
    final val seed: Long = params.seed
    final val numTrees: Int = params.numTrees
    final val maxSamples: Double = params.maxSamples
    final val maxFeatures: Double = params.maxFeatures
    final val maxDepth: Int = params.maxDepth
    final val contamination: Double = params.contamination
    final val bootstrap: Boolean = params.bootstrap
    final val anomalyScoreCol: String = params.outputFeatureName

    import IForest._
    import model.pipelines.tools.DefaultTools._

    lazy val rng = new Random(seed)

    var numSamples = 0L
    var possibleMaxSamples = 0

    val EulerConstant = 0.5772156649

    /**
     * Sample and split data to $numTrees groups, each group will build a tree.
     *
     *
     * @param dataset Training Dataset
     * @return A paired RDD, where key is the tree index, value is an array of data instances for training a iTree.
     */
    private def splitData(dataset: Dataset[_]): RDD[(Int, Array[Vector])] = {
        numSamples = dataset.count()
        val fraction =
            if ((maxSamples) > 1) (maxSamples) / numSamples
            else (maxSamples)

        require(fraction <= 1.0, "The max samples must be less than total number of the input data")

        possibleMaxSamples = (fraction * numSamples).toInt

        val advancedRgn = new RandomDataGenerator(
            RandomGeneratorFactory.createRandomGenerator(new java.util.Random(rng.nextLong()))
        )

        val rddPerTree = {
            // SampleIndices is a two-dimensional array, that generates sampling row indices in each iTree.
            // E.g. [[1, 3, 6, 4], [6, 4, 2, 5]] indicates that the first tree has data consists of the 1, 3, 6, 4 row
            // samples, the second tree has data consists of the 6, 4, 3, 5 row samples. If bootstrap is true, each
            // array can stores the repeated row indices; if false, each array contains different row indices, and each
            // index is elected with the same probability using reservoir sample method.
            // Note: sampleIndices will occupy about maxSamples * numTrees * 8 byte memory in the driver.
            val sampleIndices = if (bootstrap) {
                Array.tabulate(numTrees) { i =>
                    Array.fill(possibleMaxSamples) {
                        advancedRgn.nextLong(0, numSamples)
                    }
                }
            } else {
                Array.tabulate(numTrees) { i =>
                    reservoirSampleAndCount(Range.Long(0, numSamples, 1).iterator,
                        possibleMaxSamples, rng.nextLong)._1
                }
            }

            // rowInfo structure is a Map in which key is rowId identifying each data instance,
            // and value is a SparseVector that indicating this data instance is sampled for training which iTrees.
            // SparseVector is constructed by (numTrees, treeIdArray, numCopiesArray), where
            //  - treeIdArray indicates that which tree this row data is trained on;
            //  - numCopiesArray indicates how namy copies of this row data in the corresponding tree.
            //
            // E.g., Map{1 -> SparseVector(100, [1, 3, 5], [3, 6, 1])} means that there are 100
            // trees to construct a forest, and 3 copies of 1st row data trained on the 1 tree,
            // 6 copies trained on the 3rd tree and 1 copy trained on the 5th tree.
            val rowInfo = sampleIndices.zipWithIndex.flatMap {
                case (indices: Array[Long], treeId: Int) => indices.map(rowIndex => (rowIndex, treeId))
            }.groupBy(_._1).mapValues(x => x.map(_._2)).map {
                case (rowIndex: Long, treeIdArray: Array[Int]) =>
                    val treeIdWithNumCopies = treeIdArray.map(treeId => (treeId, 1.0))
                            .groupBy(_._1).map {
                        case (treeId: Int, tmp: Array[Tuple2[Int, Double]]) =>
                            tmp.reduce((x, y) => (treeId, x._2 + y._2))
                    }.toSeq
                    (rowIndex, Vectors.sparse(numTrees, treeIdWithNumCopies))
            }

            val broadRowInfo = dataset.sparkSession.sparkContext.broadcast(rowInfo)

            // First get all the instances in the rowInfo.
            // Then for each row, get the number of copies in each tree, copy this point
            // to an array with corresponding tree id.
            // Finally reduce by the tree id key.
            dataset.select(col((featuresCol))).rdd.map {
                case Row(point: Vector) => point
            }.zipWithIndex().filter{ case (point: Vector, rowIndex: Long) =>
                broadRowInfo.value.contains(rowIndex)
            }.flatMap { case (point: Vector, rowIndex: Long) =>
                val numCopiesInEachTree = broadRowInfo.value(rowIndex).asInstanceOf[SparseVector]
                numCopiesInEachTree.indices.zip(numCopiesInEachTree.values).map {
                    case (treeId: Int, numCopies: Double) =>
                        (treeId, Array.fill(numCopies.toInt)(point))
                }
            }.reduceByKey((arr1, arr2) => arr1 ++ arr2)
        }
        rddPerTree
    }

    /**
     * Training an iforest model for a given dataset
     *
     * @param dataset Input data which is a dataset with n_samples rows. This dataset must have a
     *                column named features, or call setFeaturesCol to set user defined feature
     *                column name. This column stores the feature values for each instance, users can
     *                use VectorAssembler to generate a feature column.
     * @return trained iforest model with an array of each tree's root node.
     */
    def fit(dataset: Dataset[_]): IForestModel = {
        val rddPerTree = splitData(dataset)

        // Each iTree of the iForest will be built on parallel and collected in the driver.
        // Approximate memory usage for iForest model is calculated, a warning will be raised if iForest is too large.
        val usageMemery = (numTrees) * 2 * possibleMaxSamples * 32 / (1024 * 1024)
        if (usageMemery > 256) {
            logger.warn("The isolation forest stored on the driver will exceed 256M memory. " +
                    "If your machine can not bear memory consuming, please try small numTrees or maxSamples.")
        }

        // build each tree and construct a forest
        val _trees = rddPerTree.map {
            case (treeId: Int, points: Array[Vector]) =>
                // Create a random for iTree generation
                val random = new Random(rng.nextInt + treeId)

                // sample features
                val (trainData, featureIdxArr) = sampleFeatures(points, maxFeatures, random)

                // calculate actual maxDepth to limit tree height
                val longestPath = Math.ceil(Math.log(Math.max(2, points.length)) / Math.log(2)).toInt
                val possibleMaxDepth = if ((maxDepth) > longestPath) longestPath else (maxDepth)
                if(possibleMaxDepth != (maxDepth)) {
                    logger.warn("building itree using possible max depth " + possibleMaxDepth + ", instead of " + (maxDepth))
                }

                val numFeatures = trainData.head.size
                // a array stores constant features index
                val constantFeatures = Array.tabulate(numFeatures + 1) {
                    i => i
                }
                // last position's value indicates constant feature offset index
                constantFeatures(numFeatures) = 0
                // build a tree
                iTree(trainData, 0, possibleMaxDepth, constantFeatures, featureIdxArr, random)

        }.collect()
        new IForestModel(_trees)
    }

    /**
     * Predict if a particular sample is an outlier or not.
     * @param dataset Input data which is a dataset with n_samples rows. This dataset must have a
     *                column named features, or call setFeaturesCol to set user defined feature
     *                column name. This column stores the feature values for each instance, users can
     *                use VectorAssembler to generate a feature column.
     * @return A predicted dataframe with a prediction column which stores prediction values.
     */
    def transform(dataset: DataFrame, model: IForestModel, inputFeatureNames: List[String])
                 (implicit spark: SparkSession, sharedParams: SharedParams): DataFrame = {
        val numSamples = dataset.count()
        val possibleMaxSamples =
            if (maxSamples > 1.0) maxSamples else maxSamples * numSamples
        spark.sparkContext.broadcast(model)
        // calculate anomaly score
        val scoreUDF = udf {
            (features: Vector) => {
                val normFactor = avgLength(possibleMaxSamples)
                val avgPathLength = calAvgPathLength(features, model)
                Math.pow(2, -avgPathLength / normFactor)
            }
        }
        // By default, we use top-3 features to explain why the point has been classified as an outlier.
        val numExplanations = math.min(inputFeatureNames.length,sharedParams.numFeaturesForExplain)
        // generate explanations
        val explanationsUDF = udf{
            (features: Vector) =>{
                val normFactor = avgLength(possibleMaxSamples)
                val featureImportance = calFeatureImportance(features, model, normFactor)
                featureImportance.argSort.reverse.take(numExplanations).map(x => inputFeatureNames(x)).mkString(",")
            }
        }
        // append a score column
        var result = dataset.withColumn(anomalyScoreCol, scoreUDF(col(featuresCol)))
        sharedParams.columeTracking.addToResult(anomalyScoreCol)
        if(sharedParams.runExplanations){
            result = result.withColumn(anomalyScoreCol + "_explanation", explanationsUDF(col(featuresCol)))
            sharedParams.columeTracking.addToResult(anomalyScoreCol + "_explanation")
        }
        result
    }


    /**
     * Calculate an average path length for a given feature set in a forest.
     * @param features A Vector stores feature values.
     * @return Average path length.
     */
    private def calAvgPathLength(features: Vector, model: IForestModel): Double = {
        val avgPathLength = model.trees.map(ifNode => {
            calPathLength(features, ifNode, 0)
        }).sum / model.trees.length
        avgPathLength
    }

    /**
     * Calculate a path langth for a given feature set in a tree.
     *
     * @param features A Vector stores feature values.
     * @param ifNode Tree's root node.
     * @param currentPathLength Current path length.
     * @return Path length in this tree.
     */
    @scala.annotation.tailrec
    private def calPathLength(features: Vector,
                              ifNode: IFNode,
                              currentPathLength: Int): Double = ifNode match {
        case leafNode: IFLeafNode => currentPathLength + avgLength(leafNode.numInstance)
        case internalNode: IFInternalNode =>
            val attrIndex = internalNode.featureIndex
            if (features(attrIndex) < internalNode.featureValue) {
                calPathLength(features, internalNode.leftChild, currentPathLength + 1)
            } else {
                calPathLength(features, internalNode.rightChild, currentPathLength + 1)
            }
    }

    /**
     * A function to calculate an expected path length with a specific data size.
     * @param size Data size.
     * @return An expected path length.
     */
    private def avgLength(size: Double): Double = {
        if (size > 2) {
            val H = Math.log(size - 1) + EulerConstant
            2 * H - 2 * (size - 1) / size
        }
        else if (size == 2) 1.0
        else 0.0
    }

    /**
     * Calculate feature importance.
     * @param features A Vector stores feature values.
     * @return an array of doubles representing the feature importance.
     */
    private def calFeatureImportance(features: Vector,
                                     model: IForestModel,
                                     normFactor: Double): Array[Double] = {
        model.trees.map(ifNode => {
            var importancePerTree = Array.fill(features.size)(0.0)
            val emptyList = List()
            val results = calPathLength(features, ifNode, 0, emptyList)
            val curScore = Math.pow(2, -results._1 / normFactor)
            results._2.foreach(x => (importancePerTree(x) = curScore))
//            println(importancePerTree.mkString(","))
            importancePerTree
        }).reduce((a,b) => {a.zip(b).map{case(x,y) => x+y}}).map(x => x / model.trees.length)
    }

    /**
     * Calculate a path length for a given feature set in a tree.
 *
     * @param features A Vector stores feature values.
     * @param ifNode Tree's root node.
     * @param currentPathLength Current path length.
     * @return Path length in this tree.
     */
    @scala.annotation.tailrec
    private def calPathLength(features: Vector,
                              ifNode: IFNode,
                              currentPathLength: Int,
                              usedFeatures: List[Int]): (Double, List[Int]) = ifNode match {
        case leafNode: IFLeafNode => (currentPathLength + avgLength(leafNode.numInstance), usedFeatures)
        case internalNode: IFInternalNode =>
            val attrIndex = internalNode.featureIndex
            val newUsedFeatures = attrIndex :: usedFeatures
            if (features(attrIndex) < internalNode.featureValue) {
                calPathLength(features, internalNode.leftChild, currentPathLength + 1, newUsedFeatures)
            } else {
                calPathLength(features, internalNode.rightChild, currentPathLength + 1, newUsedFeatures)
            }
    }


    /**
     * Sample features to train a tree.
     * @param data Input data to train a tree, each element is an instance.
     * @param maxFeatures The number of features to draw.
     * @return Tuple (sampledFeaturesDataset, featureIdxArr),
     *         featureIdxArr is an array stores the origin feature idx before the feature sampling
     */
    def sampleFeatures(data: Array[Vector],
                       maxFeatures: Double,
                       random: Random = new Random()): (Array[Array[Double]], Array[Int]) = {

        // get feature size
        val numFeatures = data.head.size
        // calculate the number of sampling features
        val subFeatures: Int =
            if (maxFeatures <= 1) (maxFeatures * numFeatures).toInt
            else if (maxFeatures > numFeatures) {
                logger.warn("maxFeatures is larger than the numFeatures, using all features instead")
                numFeatures
            }
            else maxFeatures.toInt

        if (subFeatures == numFeatures) {
            (data.toArray.map(vector => vector.asInstanceOf[DenseVector].values), Array.range(0, numFeatures))
        } else {
            // feature index for sampling features
            val featureIdx = random.shuffle[Int, IndexedSeq](0 until numFeatures).take(subFeatures)

            val sampledFeatures = mutable.ArrayBuilder.make[Array[Double]]
            data.foreach(vector => {
                val sampledValues = new Array[Double](subFeatures)
                featureIdx.zipWithIndex.foreach(elem => sampledValues(elem._2) = vector(elem._1))
                sampledFeatures += sampledValues
            })
            (sampledFeatures.result(), featureIdx.toArray)
        }
    }

    /**
     * Builds a tree
     *
     * @param data Input data, a two dimensional array, can be regarded as a table, each row
     *             is an instance, each column is a feature value.
     * @param currentPathLength current node's path length
     * @param maxDepth height limit during building a tree
     * @param constantFeatures an array stores constant features indices, constant features
     *                         will not be drawn
     * @param featureIdxArr an array stores the mapping from the sampled feature idx to the origin feature idx
     * @param random random for generating iTree
     * @return tree's root node
     */
    def iTree(data: Array[Array[Double]],
              currentPathLength: Int,
              maxDepth: Int,
              constantFeatures: Array[Int],
              featureIdxArr: Array[Int],
              random: Random): IFNode = {

        var constantFeatureIndex = constantFeatures.last
        // the condition of leaf node
        // 1. current path length exceeds max depth
        // 2. the number of data can not be splitted again
        // 3. there are no non-constant features to draw
        if (currentPathLength >= maxDepth || data.length <= 1) {
            new IFLeafNode(data.length)
        } else {
            val numFeatures = data.head.length
            var attrMin = 0.0
            var attrMax = 0.0
            var attrIndex = -1
            // until find a non-constant feature
            var findConstant = true
            while (findConstant && numFeatures != constantFeatureIndex) {
                // select randomly a feature index
                val idx = random.nextInt(numFeatures - constantFeatureIndex) + constantFeatureIndex
                attrIndex = constantFeatures(idx)
                val features = Array.tabulate(data.length)( i => data(i)(attrIndex))
                attrMin = features.min
                attrMax = features.max
                if (attrMin == attrMax) {
                    // swap constant feature index with non-constant feature index
                    constantFeatures(idx) = constantFeatures(constantFeatureIndex)
                    constantFeatures(constantFeatureIndex) = attrIndex
                    // constant feature index add 1, then update
                    constantFeatureIndex += 1
                    constantFeatures(constantFeatures.length - 1) = constantFeatureIndex
                } else {
                    findConstant = false
                }
            }
            if (numFeatures == constantFeatureIndex) new IFLeafNode(data.length)
            else {
                // select randomly a feature value between (attrMin, attrMax)
                val attrValue = random.nextDouble() * (attrMax - attrMin) + attrMin
                // split data according to the attrValue
                val leftData = data.filter(point => point(attrIndex) < attrValue)
                val rightData = data.filter(point => point(attrIndex) >= attrValue)
                // recursively build a tree
                new IFInternalNode(
                    iTree(leftData, currentPathLength + 1, maxDepth, constantFeatures.clone(), featureIdxArr, random),
                    iTree(rightData, currentPathLength + 1, maxDepth, constantFeatures.clone(), featureIdxArr, random),
                    featureIdxArr(attrIndex), attrValue)
            }
        }
    }

    /**
     * Reservoir sampling implementation that also returns the input size.
     * @param input input size
     * @param k reservoir size
     * @param seed random seed
     * @return (samples, input size)
     */
    def reservoirSampleAndCount[T: ClassTag](input: Iterator[T],
                                             k: Int,
                                             seed: Long = Random.nextLong()): (Array[T], Long) = {
        val reservoir = new Array[T](k)
        // Put the first k elements in the reservoir.
        var i = 0
        while (i < k && input.hasNext) {
            val item = input.next()
            reservoir(i) = item
            i += 1
        }

        // If we have consumed all the elements, return them. Otherwise do the replacement.
        if (i < k) {
            // If input size < k, trim the array to return only an array of input size
            val trimReservoir = new Array[T](i)
            System.arraycopy(reservoir, 0, trimReservoir, 0, i)
            (trimReservoir, i)
        } else {
            // If input size > k, continue the sampling process.
            var l = i.toLong
            val rand = new Random(seed)
            while (input.hasNext) {
                val item = input.next()
                l += 1
                // There are k elements in the reservoir, and the l-th element has been
                // consumed. It should be chosen with probability k/l. The expression
                // below is a random long chosen uniformly from [0,l)
                val replacementIndex = (rand.nextDouble() * l).toLong
                if (replacementIndex < k) {
                    reservoir(replacementIndex.toInt) = item
                }
            }
            (reservoir, l)
        }
    }
}

object IForest{
    val logger: Logger = Logger.getLogger(IForest.getClass)
}
