package model.pipelines.tools

import model.common.SubspaceParams

import scala.collection.mutable.ListBuffer
import scala.util.Random
import scala.util.control.Breaks.{break, breakable}

object DefaultTools {
    implicit class ArrayExtension[T <% Ordered[T]](val array: Array[T]) {
        def argSort = {
            array.zipWithIndex.sortBy(_._1).map(_._2).toArray
        }
    }

    implicit def between(minInclusive: Int, maxInclusive: Int, rng: Random)={
        minInclusive + rng.nextInt((maxInclusive - minInclusive) + 1)
    }

    def generateSubspaces(inputFeatureNames: List[String],
                          params: SubspaceParams
                         ) = {
        params.subspaceMaxDim = Math.min(params.subspaceMaxDim, inputFeatureNames.length)
        lazy val rng = new Random(params.seed)
        var results = new ListBuffer[List[String]]()
        var existing = Set[String]()
        while(results.length < params.subspaceNumSpaces){
            val curList = rng.shuffle(inputFeatureNames)
                    .take(between(params.subspaceMinDim,params.subspaceMaxDim, rng))
                    .sorted
            val curListInStr = curList.mkString(",")
            breakable {
                if (existing.contains(curListInStr))
                    break
                results += curList
                existing += curListInStr
            }
        }
        if(params.useFullSpace)
            results += inputFeatureNames
        results.toList
    }
}
