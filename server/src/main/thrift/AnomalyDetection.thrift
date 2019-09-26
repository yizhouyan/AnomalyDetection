/*
  The thrift file specifies the structs and functions that are shared
  among AnomalyDetection projects. Thrift automatically generates classes to manipulate
  the data in each language.

  Currently, we support Java and Scala clients. We will support python clients in later versions.
*/


namespace java anomalydetection
namespace scala anomalydetection

/*
  A HyperParameter guides the fitting of a TransformerSpec to a DataFrame in order to produce a Transformer.
  Some example hyperparameters include the number of trees in a random forest, the regularization parameter in
  linear regression, or the value "k" in k-means clustering.

  name: The name of this hyperparameter.
  value: The value assigned to this hyperparameter.
  type: The type of data stored in the hyperparameter (e.g. Integer, String).
  min: (for numeric hyperparameters only) The minimum value allowed for this hyperparameter.
  max: (for numeric hyperparameters only) The maximum value allowed for this hyperparameter.
*/
struct HyperParameter {
  1: string name,
  2: string value,
  3: string type,
  4: double min,
  5: double max
}