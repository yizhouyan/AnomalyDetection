-- Top level grouping mechanism for machine learning events.
-- This project stores multiple projects, each of which contains multiple
-- experiment runs.
DROP TABLE IF EXISTS Project;
CREATE TABLE Project (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  -- A descriptive name for the project.
  name TEXT,
  -- The name of the project author.
  author TEXT,
  -- A description of the project and its goals.
  description TEXT,
  -- The timestamp at which the project was created.
  created TIMESTAMP NOT NULL
);

-- Each project contains many experiment runs. In experiment runs,
-- you will find the actual pipelines & events
DROP TABLE IF EXISTS ExperimentRun;
CREATE TABLE ExperimentRun (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  -- The experiment which contains this run.
  project INTEGER REFERENCES Project NOT NULL,
  -- A description of this particular run, with the goals and parameters it used.
  description TEXT,
  -- A timestamp indicating the time at which this experiment run was created.
  sha TEXT,
  -- Commit hash of the code for this run
  created TIMESTAMP NOT NULL
);

-- A DataFrame is a primitive. It is a table of data with named and typed columns.
DROP TABLE IF EXISTS DataFrame;
CREATE TABLE DataFrame (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  -- User assigned content associated with the data frame
  tag TEXT,
  -- The ExperimentRun that contains this DataFrame
  experimentRun INTEGER REFERENCES ExperimentRun NOT NULL,
  -- A path to the file where this DataFrame is stored
  filepath TEXT
);

-- A single column in a DataFrame
-- Each column has a unique name and can only contain a single type.
DROP TABLE IF EXISTS DataFrameColumn;
CREATE TABLE DataFrameColumn (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  -- The ID of the DataFrame that has this column
  dfId INTEGER REFERENCES DataFrame NOT NULL,
  -- The name of the column
  name TEXT NOT NULL,
  -- The type of data that is stored in this column: e.g: Integer, String
  type TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS DataFrameColumnIndexDfId ON DataFrameColumn(dfId);

-- An EventSpec is a primitive that describes
-- the hyperparameters used to create a model.
DROP TABLE IF EXISTS EstimatorSpec;
CREATE TABLE EstimatorSpec (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  -- The kind of Transformer/Fit event that this spec describes (e.g. Isolation Forest)
  estimatorType TEXT NOT NULL,
  -- User assigned content about this spec
  tag TEXT,
  -- The experiment run in which this spec is contained
  experimentRun INTEGER REFERENCES ExperimentRun NOT NULL
);

-- A hyperparameter helps guide the fitting of a model.
-- e.g. Number of trees in an isolation forest,
DROP TABLE IF EXISTS HyperParameter;
CREATE TABLE HyperParameter (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  -- The TransformerSpec that contains this hyperparameter
  spec INTEGER REFERENCES EstimatorSpec NOT NULL,
  -- The name of this hyperparameter
  paramName TEXT NOT NULL,
  -- The type of the hyperparameter (e.g. String, Integer)
  paramType VARCHAR(40) NOT NULL,
  -- The value assigned to this hyperparameter
  paramValue TEXT NOT NULL,
  -- The ExperimentRun that contains this hyperparameter
  experimentRun INTEGER REFERENCES ExperimentRun NOT NULL
);

-- Transformers are machine learning primitives that take an input
-- DataFrame and produce an output DataFrame
DROP TABLE IF EXISTS Model;
CREATE TABLE Model (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  --  The kind of Model (e.g. GBDT)
  modelType TEXT NOT NULL,
  --  User assigned text to describe this Transformer
  tag TEXT,
  -- The ExperimentRun that contains this Transformer
  experimentRun INTEGER REFERENCES ExperimentRun NOT NULL,
  --  The path to the serialized Transformer
  filepath TEXT NOT NULL
);

-- Describes a Fit Event - Fitting an EstimatorSpec to a DataFrame to
-- produce a model
DROP TABLE IF EXISTS FitEvent;
CREATE TABLE FitEvent (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  -- The estimatorSpec guiding the fitting
  estimatorSpec INTEGER REFERENCES EstimatorSpec NOT NULL,
  -- The model (fitted Transformer) produced by the fitting
  model INTEGER REFERENCES Model NOT NULL,
  -- The DataFrame that the Spec is being fitted to
  df INTEGER REFERENCES DataFrame NOT NULL,
  -- The columns in the input DataFrame that are used by the transformer
  inputColumns TEXT NOT NULL, -- Should be comma-separated, no spaces, alphabetical.
  -- The name of the columns in the DataFrame whose values this Transformer is supposed to predict. We support
  -- multiple label columns.
  labelColumns TEXT NOT NULL,
  -- The ExperimentRun that contains this event.
  experimentRun INTEGER REFERENCES ExperimentRun NOT NULL,
  -- The stageNumber of the event in the pipeline
  stageNumber INTEGER DEFAULT -1
);
CREATE INDEX IF NOT EXISTS FitEventIndexModel ON FitEvent(model);

-- A TransformEvent describes using a Model to produce an output
-- DataFrame from an input DataFrame
DROP TABLE IF EXISTS TransformEvent;
CREATE TABLE TransformEvent (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  -- The original DataFrame that is input into the Transformer
  oldDf INTEGER REFERENCES DataFrame NOT NULL,
  -- The output DataFrame of the Transformer
  newDf INTEGER REFERENCES DataFrame NOT NULL,
  -- The Transformer used to perform this transformation
  model INTEGER REFERENCES Model,
  -- The columns in the input DataFrame that are used by the transformer
  inputColumns TEXT NOT NULL, -- Should be comma-separated, no spaces, alphabetical.
  -- The columns outputted by the Transformer
  outputColumns TEXT NOT NULL, -- Should be comma-separated, no spaces, alphabetical.
  -- The names of the output column that will contain the model's prediction
  predictionColumn TEXT NOT NULL,
  -- The ExperimentRun that contains this event.
  experimentRun INTEGER REFERENCES ExperimentRun NOT NULL,
  -- The stageNumber of the event in the pipeline
  stageNumber INTEGER DEFAULT -1
);
CREATE INDEX IF NOT EXISTS TransformEventIndexNewDf ON TransformEvent(newDf);
CREATE INDEX IF NOT EXISTS TransformEventIndexExperimentRun ON TransformEvent(experimentRun);

-- A UnsupervisedEvent describes using an EstimateSpec to produce an output
-- DataFrame from an input DataFrame
DROP TABLE IF EXISTS UnsupervisedEvent;
CREATE TABLE UnsupervisedEvent (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  -- The original DataFrame that is input into the Transformer
  oldDf INTEGER REFERENCES DataFrame NOT NULL,
  -- The output DataFrame of the Transformer
  newDf INTEGER REFERENCES DataFrame NOT NULL,
  -- The EstimatorSpec used to perform this transformation
  estimatorSpec INTEGER REFERENCES EstimatorSpec,
  -- The columns in the input DataFrame that are used by the transformer
  inputColumns TEXT NOT NULL, -- Should be comma-separated, no spaces, alphabetical.
  -- The columns outputted by the Transformer
  outputColumns TEXT NOT NULL, -- Should be comma-separated, no spaces, alphabetical.
  -- The ExperimentRun that contains this event.
  experimentRun INTEGER REFERENCES ExperimentRun NOT NULL,
  -- The stageNumber of the event in the pipeline
  stageNumber INTEGER DEFAULT -1
);
CREATE INDEX IF NOT EXISTS UnsupervisedEventIndexNewDf ON UnsupervisedEvent(newDf);
CREATE INDEX IF NOT EXISTS UnsupervisedEventIndexExperimentRun ON UnsupervisedEvent(experimentRun);

-- An ExampleSelectorEvent describes using an EstimateSpec to produce an output
-- DataFrame from an input DataFrame
DROP TABLE IF EXISTS ExampleSelectorEvent;
CREATE TABLE ExampleSelectorEvent (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    -- The data DataFrame that is input into the Transformer
    oldDf INTEGER REFERENCES DataFrame NOT NULL,
    -- The label DataFrame that is input into the Transformer
    labelDf INTEGER REFERENCES DataFrame NOT NULL,
    -- The output DataFrame of the Transformer
    newDf INTEGER REFERENCES DataFrame NOT NULL,
    -- The EstimatorSpec used to perform this transformation
    estimatorSpec INTEGER REFERENCES EstimatorSpec,
    -- The ExperimentRun that contains this event.
    experimentRun INTEGER REFERENCES ExperimentRun NOT NULL,
    -- The stageNumber of the event in the pipeline
    stageNumber INTEGER DEFAULT -1
);
CREATE INDEX IF NOT EXISTS ExampleSelectorEventIndexNewDf ON ExampleSelectorEvent(newDf);
CREATE INDEX IF NOT EXISTS ExampleSelectorEventIndexExperimentRun ON ExampleSelectorEvent(experimentRun);

-- An event that represents the evaluation of a model given a DataFrame
DROP TABLE IF EXISTS ModelMetricEvent;
CREATE TABLE ModelMetricEvent (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  -- The model being evaluated
  model INTEGER REFERENCES Model NOT NULL,
  -- The DataFrame that the model is being evaluated on
  df INTEGER REFERENCES DataFrame NOT NULL,
  -- The type of Metric being measured (e.g. Squared Error, Accuracy, f1)
  metricType TEXT NOT NULL,
  -- The value of the measured Metric
  metricValue REAL NOT NULL,
  -- The Experiment Run that contains this Metric
  experimentRun INTEGER REFERENCES ExperimentRun NOT NULL
);

-- An event that represents the evaluation of an unsupervised model given a DataFrame
DROP TABLE IF EXISTS UnsupervisedMetricEvent;
CREATE TABLE UnsupervisedMetricEvent (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  -- The estimator parameters used to evaluate
  estimatorSpec INTEGER REFERENCES EstimatorSpec NOT NULL,
  -- The DataFrame that the model is being evaluated on
  df INTEGER REFERENCES DataFrame NOT NULL,
  -- The type of Metric being measured (e.g. Squared Error, Accuracy, f1)
  metricType TEXT NOT NULL,
  -- The value of the measured Metric
  metricValue REAL NOT NULL,
  -- The Experiment Run that contains this Metric
  experimentRun INTEGER REFERENCES ExperimentRun NOT NULL
);

-- A generic Event that can represent anything
DROP TABLE IF EXISTS Event;
CREATE TABLE Event (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  -- The type of the event that this entry represents (FitEvent,
  -- TransformEvent or UnsupervisedEvent)
  eventType TEXT NOT NULL,
  -- The id of the event within its respective table
  eventId INTEGER NOT NULL, -- references the actual event in the table
  -- The stage number of the event in the pipeline. Events with the same stage numbers will be
  -- executed together.
  stageNumber INTEGER NOT NULL,
  -- The Experiment Run that contains this Event
  experimentRun INTEGER REFERENCES ExperimentRun NOT NULL
);