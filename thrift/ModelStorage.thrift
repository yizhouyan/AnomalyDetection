/*
  The thrift file specifies the structs and functions that are shared
  among AnomalyDetection projects. Thrift automatically generates classes to manipulate
  the data in each language.

  Currently, we support Java and Scala clients. We will support python clients in later versions.
  Revised based on code from ModelDB: https://github.com/mitdbg/modeldb
*/


namespace java anomalydetection
namespace scala anomalydetection

/*
  A project is the highest level in the grouping hierarchy. A project can
  contain multiple experiment runs.

  Attributes:
  id: A unique identifier for this project.
  name: The name of this project.
  author: The name of the author, the person who created the project.
  description: A human readable description of the project - i.e. it's goals and methods.

  Note that in order to flatten this into tables, the experiments contained within this
  project are not in the project struct.
*/
struct Project {
  1: i32 id = -1,
  2: string name,
  3: string author,
  4: string description,
  5: optional string created
}

/*
  Experiment runs are contained within projects. Note that the
  project must be specified, even if it is the default experiment.

  id: a unique identifier for this run.
  project: The id of the project that contains this run.
  description: User assigned text to this experiment run. Can be used to summarize
    data, method, etc.
  sha: Commit hash of the code used in the current run.
  created: Timestamp for when the ExperimentRun was created.
*/
struct ExperimentRun {
  1: i32 id = -1,
  2: i32 project,
  3: string description,
  4: optional string sha,
  5: optional string created
}

/*
  A column in a DataFrame.

  name: The name of this column.
  type: The type of data stored in this column (e.g. Integer, String).
*/
struct DataFrameColumn {
  1: string name,
  2: string type
}

/*
  A tabular set of data. Contains many columns, each containing a single type
  of data. Each row in a DataFrame is a distinct example in the dataset.

  id: A unique identifier for this DataFrame.
  schema: The columns in the DataFrame.
  tag: Short, human-readable text to identify this DataFrame.
  filepath: The path to the file that contains the data in this DataFrame.
*/
struct DataFrame {
  1: i32 id = -1, // when unknown
  2: list<DataFrameColumn> schema,
  3: string tag = "",
  4: string filepath
}


/*
  A HyperParameter guides the fitting of a TransformerSpec to a DataFrame in order to produce a Transformer.
  Some example hyperparameters include the number of trees in a random forest, the regularization parameter in
  linear regression, or the value "k" in k-means clustering.

  name: The name of this hyperparameter.
  value: The value assigned to this hyperparameter.
  type: The type of data stored in the hyperparameter (e.g. Integer, String).
*/
struct HyperParameter {
  1: string name,
  2: string value,
  3: string type
}

/*
  An event that represents the creation of a project.

  project: The project to be created by this event.
*/
struct ProjectEvent {
  1: Project project
}

/*
  The server's response to creating a project.

  projectId: The id of the created project.
*/
struct ProjectEventResponse {
  1: i32 projectId
}

/*
  An event representing the creation of an Experiment Run.

  experimentRun: The ExperimentRun to create.
*/
struct ExperimentRunEvent {
  1: ExperimentRun experimentRun
}

/*
  The response given to the creation of an Experiment Run.

  experimentRunId: The id of the created ExperimentRun.
*/
struct ExperimentRunEventResponse {
  1: i32 experimentRunId
}

/*
  An EstimatorSpec is a machine learning primitive that describes
  the hyperparameters used to create a model.

  id: A unique identifier for this EstimatorSpec.
  estimatorType: The type of the Estimator that is created by using this EstimatorSpec (e.g. isolation forest).
  hyperparameters: The hyperparameters that guide this spec.
  tag: User assigned content associated with this Spec.
*/
struct EstimatorSpec {
  1: i32 id = -1,
  2: string estimatorType,
  3: list<HyperParameter> hyperparameters,
  4: string tag = ""
}

/*
  A Model is a machine learning primitive that takes in a DataFrame and
  outputs another DataFrame.

  id: A unique identifier for this transformer.
  transformerType: The type of transformer this is (e.g. linear regression).
  tag: User assigned content associated with this transformer.
  filepath: The path to the serialized version of this transformer.
*/
struct Model {
  1: i32 id = -1,
  // modelType is present in spec as well as transformer
  // because some models may not have a spec associated with them
  2: string modelType,
  3: string tag ="",
  4: string filepath
}

/*
  Represents the fitting of a DataFrame with a EstimatorSpec to produce a Model.

  df: The DataFrame that is being fitted.
  spec: The EstimatorSpec guiding the fitting.
  model: The model produced by the fitting.
  featureColumns: The names of the features (i.e. columns of the DataFrame) used by the Transformer.
  labelColumns: The columns of the DataFrame that contains the label (i.e. true prediction value) associated with
    each example.
  experimentRunId: The id of the ExperimentRun which contains this event.
*/
struct FitEvent {
  1: DataFrame df,
  2: EstimatorSpec spec,
  3: Model model,
  4: list<string> featureColumns,
  5: list<string> labelColumns,
  6: i32 experimentRunId,
  7: i32 stageNumber
}

/*
  The response created after a FitEvent occurs.

  dfId: The id of the DataFrame referenced by the fit event.
  specId: The id of the TransformerSpec that guided the fit.
  modelId: The id of the outputted Transformer.
  eventId: The generic event id of this fit event (unique among all events).
  fitEventId: The specific FitEvent id of the created fit event (unique among fit events).
*/
struct FitEventResponse {
  1: i32 dfId,
  2: i32 specId,
  3: i32 modelId,
  4: i32 eventId,
  5: i32 fitEventId
}

/*
  This event indicates that an output DataFrame was created from an input DataFrame using a Model.

  oldDataFrame: The input DataFrame.
  newDataFrame: The output DataFrame.
  model: The Model that produced the output DataFrame from the input DataFrame.
  inputColumns: The columns of the input DataFrame that the Transformer depends on.
  outputColumns: The columns that the Model outputs in the new DataFrame.
  predictionColumn: The column name of the prediction.
  experimentRunId: The id of the Experiment Run that contains this event.
*/
struct TransformEvent {
  1: DataFrame oldDataFrame,
  2: DataFrame newDataFrame,
  3: Model model
  4: list<string> inputColumns,
  5: list<string> outputColumns,
  6: string predictionColumn,
  7: i32 experimentRunId,
  8: i32 stageNumber
}

/*
  The response given to the creation of a TransformEvent.

  oldDataFrameId: The id of the input DataFrame of the transformation.
  newDataFrameId: The id of the output DataFrame of the transformation.
  modelId: The id of the Model that performed the transformation.
  eventId: The generic event id of this transform event (unique among all events).
  transformEventId: The specific TransformEvent id of the created transform event
  (unique among transform events)
*/
struct TransformEventResponse {
  1: i32 oldDataFrameId,
  2: i32 newDataFrameId,
  3: i32 modelId,
  4: i32 eventId,
  5: i32 transformEventId
}

/*
  This event indicates that an output DataFrame was created from an input DataFrame using unsupervised learning.

  oldDataFrame: The input DataFrame.
  newDataFrame: The output DataFrame.
  estimatorSpec: The EstimatorSpec used to perform this transformation
  inputColumns: The columns of the input DataFrame that the Transformer depends on.
  outputColumns: The columns that the Model outputs in the new DataFrame.
  experimentRunId: The id of the Experiment Run that contains this event.
*/
struct UnsupervisedEvent {
  1: DataFrame oldDataFrame,
  2: DataFrame newDataFrame,
  3: EstimatorSpec estimatorSpec
  4: list<string> inputColumns,
  5: list<string> outputColumns,
  6: i32 experimentRunId,
  7: i32 stageNumber
}

/*
  The response given to the creation of a UnsupervisedEventResponse.

  oldDataFrameId: The id of the input DataFrame of the transformation.
  newDataFrameId: The id of the output DataFrame of the transformation.
  specId: The id of the EstimatorId that guided the learning.
  eventId: The generic event id of this transform event (unique among all events).
  unsupervisedEventId: The specific UnsupervisedEvent id of the created event
  (unique among unsupervised events)
*/
struct UnsupervisedEventResponse {
  1: i32 oldDataFrameId,
  2: i32 newDataFrameId,
  3: i32 specId,
  4: i32 eventId,
  5: i32 unsupervisedEventId
}

/*
  This event indicates that an output DataFrame was created from an input DataFrame using example sources/example selectors.

  oldDataFrame: The input DataFrame.
  newDataFrame: The output DataFrame.
  estimatorSpec: The EstimatorSpec used to perform this transformation.
  experimentRunId: The id of the Experiment Run that contains this event.
  labelDataFrame: The dataframe that contains the labels.
*/
struct ExampleSelectorEvent {
  1: DataFrame oldDataFrame,
  2: DataFrame labelDataFrame,
  3: DataFrame newDataFrame,
  4: EstimatorSpec estimatorSpec,
  5: i32 experimentRunId,
  6: i32 stageNumber
}

/*
  The response given to the creation of a ExampleSelectorEvent.

  oldDataFrameId: The id of the input DataFrame of the transformation.
  labelDataFrameId: The id of the label DataFrame of the transformation.
  newDataFrameId: The id of the output DataFrame of the transformation.
  specId: The id of the EstimatorId that guided the learning.
  eventId: The generic event id of this exampleSelector event (unique among all events).
  exampleSelectorEventId: The specific UnsupervisedEvent id of the created event
  (unique among unsupervised events)
*/
struct ExampleSelectorEventResponse {
  1: i32 oldDataFrameId,
  2: i32 newDataFrameId,
  3: i32 labelDataFrameId,
  4: i32 specId,
  5: i32 eventId,
  6: i32 exampleSelectorEventId
}

/*
  This event indicates that a metric (e.g. accuracy) was evaluated on a DataFrame using a given Model.

  df: The DataFrame used for evaluation.
  model: The model (a Transformer) being evaluated.
  metricType: The kind of metric being evaluated (e.g. accuracy, squared error).
  labelCol: The column in the original DataFrame that this metric is calculated for.
  predictionCol: The column from the predicted columns that this metric is calculated for.
  experimentRunId: The id of the Experiment Run that contains this event.
*/
struct ModelMetricEvent {
  1: DataFrame df,
  2: Model model,
  3: string metricType,
  4: double metricValue,
  5: string labelCol,
  6: string predictionCol,
  7: i32 experimentRunId
}

/*
  The response given after creating a MetricEvent.

  modelId: The id of the Transformer for which this metric was calculated for.
  dfId: The id of the DataFrame used for this calculation.
  eventId: The generic event id of the created event (unique across all events).
  metricEventId: The id of the MetricEvent (unique across all ModelMetricEvent).
*/
struct ModelMetricEventResponse {
  1: i32 modelId,
  2: i32 dfId,
  3: i32 eventId,
  4: i32 modelMetricEventId
}

/*
  This event indicates that a metric (e.g. accuracy) was evaluated on a DataFrame using a given Model.

  df: The DataFrame used for evaluation.
  estimatorSpec: The estimator parameters used to evaluate.
  metricType: The kind of metric being evaluated (e.g. accuracy, squared error).
  labelCol: The column in the original DataFrame that this metric is calculated for.
  predictionCol: The column from the predicted columns that this metric is calculated for.
  experimentRunId: The id of the Experiment Run that contains this event.
*/
struct UnsupervisedMetricEvent {
  1: DataFrame df,
  2: EstimatorSpec estimatorSpec,
  3: string metricType,
  4: double metricValue,
  5: string labelCol,
  6: string predictionCol,
  7: i32 experimentRunId
}

/*
  The response given after creating a MetricEvent.

  specId: The id of the EstimatorId that guided the learning.
  dfId: The id of the DataFrame used for this calculation.
  eventId: The generic event id of the created event (unique across all events).
  unsupervisedMetricEventId: The id of the UnsupervisedMetricEvent (unique across all UnsupervisedMetricEvent).
*/
struct UnsupervisedMetricEventResponse {
  1: i32 specId,
  2: i32 dfId,
  3: i32 eventId,
  4: i32 unsupervisedMetricEventId
}

/*
  The response that indicates all the experiment runs for a project.

  projId: The id of the project.
  experimentRuns: The list of experiment runs in the project.
*/
struct ProjectExperimentRuns {
  1: i32 projId,
  2: list<ExperimentRun> experimentRuns
}

/*
 Thrown when an Experiment Run ID is not defined.
 */
exception InvalidExperimentRunException {
  1: string message
}

/*
 Thrown when the server has thrown an exception that we did not think of.
 */
exception ServerLogicException {
  1: string message
}

/*
 Thrown when a specified resource (e.g. DataFrame, Transformer) is not found.
 For example, if you try to read Transformer with ID 1, then we throw this
 exception if that Transformer does not exist.
 */
exception ResourceNotFoundException {
  1: string message
}

/*
 Thrown when field of a structure is empty or incorrect.
 For example, if you try to get the path for a Transformer, but the server
 finds that the path is empty, then this exception gets thrown.
 */
exception InvalidFieldException {
  1: string message
}

service ModelStorageService {
  /*
   Tests connection to the server. This just returns 200.
   */
  i32 testConnection(),

  /*
   Stores a DataFrame in the database.

   df: The DataFrame.
   experimentRunId: The ID of the experiment run that contains this given DataFrame.
   */
  i32 storeDataFrame(1: DataFrame df, 2: i32 experimentRunId)
    throws (1: InvalidExperimentRunException ierEx, 2: ServerLogicException svEx),

  /*
   Get the path to the file that contains a serialized version of the Transformer with the given ID.
   // TODO: This seems unnecessary because there's another method called getFilePath. Perhaps we should get rid of it.

   transformerId: The ID of a Transformer.
   */
  string pathForModel(1: i32 modelId)
    throws (1: ResourceNotFoundException rnfEx, 2: InvalidFieldException efEx, 3: ServerLogicException svEx),

  /*
   Stores a FitEvent in the database. This indicates that a TransformerSpec has been fit to a DataFrame to produce
   a Transformer.

   fe: The FitEvent.
   */
  FitEventResponse storeFitEvent(1: FitEvent fe)
   throws (1: InvalidExperimentRunException ierEx, 2: ServerLogicException svEx),

  /*
   Stores a MetricEvent in the database. This indicates that a Transformer was used to compute an evaluation metric
   on a DataFrame.
   */
  ModelMetricEventResponse storeModelMetricEvent(1: ModelMetricEvent me)
    throws (1: InvalidExperimentRunException ierEx, 2: ServerLogicException svEx),

  /*
     Stores a UnsupervisedMetricEvent in the database.
     */
  UnsupervisedMetricEventResponse storeUnsupervisedMetricEvent(1: UnsupervisedMetricEvent ume)
      throws (1: InvalidExperimentRunException ierEx, 2: ServerLogicException svEx),

  /*
   Stores a TransformEvent in the database. This indicates that a Transformer was used to create an output DataFrame
   from an input DataFrame.

   te: The TransformEvent.
   */
  TransformEventResponse storeTransformEvent(1: TransformEvent te)
    throws (1: InvalidExperimentRunException ierEx, 2: ServerLogicException svEx),

  /*
     Stores an UnsupervisedEvent in the database. This indicates that an unsupervised algorithm was used to create an output DataFrame
     from an input DataFrame.

     te: The UnsupervisedEvent.
   */
  UnsupervisedEventResponse storeUnsupervisedEvent(1: UnsupervisedEvent te)
      throws (1: InvalidExperimentRunException ierEx, 2: ServerLogicException svEx),

  /*
       Stores an ExampleSelectorEvent in the database. This indicates that an example source/selector was used to create an output DataFrame
       from an input DataFrame.

       te: The ExampleSelectorEvent.
   */
  ExampleSelectorEventResponse storeExampleSelectorEvent(1: ExampleSelectorEvent te)
        throws (1: InvalidExperimentRunException ierEx, 2: ServerLogicException svEx),

  /*
   Stores a ProjectEvent in the database. This indicates that a new project was created and stored in the database.

   pr: The ProjectEvent.
   */
  ProjectEventResponse storeProjectEvent(1: ProjectEvent pr)
    throws (1: ServerLogicException svEx),

  /*
   Stores an ExperimentRunEvent in the database. This indicates that a new experiment run was created and stored under
   a given experiment.

   er: The ExperimentRunEvent.
   */
  ExperimentRunEventResponse storeExperimentRunEvent(1: ExperimentRunEvent er)
    throws (1: ServerLogicException svEx),

  /*
     Compares the hyperparameters of the EstimatorSpec.

     estimatorSpecId1: The ID of a EstimatorSpec.
     estimatorSpecId2: The ID of another EstimatorSpec.
     */
  i32 compareHyperparameters(1: i32 estimatorSpecId1, 2: i32 estimatorSpecId2)
    throws (1: ResourceNotFoundException rnfEx, 2: ServerLogicException svEx),

  /*
   Get information about a project and the experiments/experiment runs that it contains.

   projId: The ID of a project.
   */
  ProjectExperimentRuns getExperimentRunsInProject(1: i32 projId) throws (1: ServerLogicException svEx),
}



