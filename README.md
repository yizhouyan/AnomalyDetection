# End-to-end Anomaly Detection System

**Website**: 

## Contents
- [Setup and Installation](#setup-and-installation)
- [Contact Us](#contact-us)

## Setup and Installation
1. **Clone the repo**
    ```bash
    git clone https://github.com/yizhouyan/AnomalyDetection.git
    ```

2. **Install dependencies**

    The system requires Linux or MacOS. The code below shows how you can install the dependencies on each of them. 
    
    Depending on the client you're using, we assume you have the following already installed:
    - spark.ml client:
        - Java 1.8+
        - Spark 2.3.3\*\*
            
    On OSX, we also assume that you have [homebrew](https://brew.sh/) installed.
        
     **On Mac OSX:**
       
        ```bash
        # Use homebrew to install the dependencies
        brew install sqlite
        brew install maven
        brew install node
        brew install sbt # for spark.ml client only
    
        # The system works only with Thrift 0.9.3 and 0.10.0. 
        `brew install thrift`
        ```
        
3. **Build**

    The Anomaly Detection System is composed of three components: the **server**, the **client libraries**, and the **frontend**.

    In the following, **[path_to_system]** refers to the directory into which you have cloned the anomaly detection system
     repo and **[thrift_version]** is 0.9.3 or 0.10.0 depending on your thrift version (check by running ```thrift -version```).

    ```bash
        # run the script to set up the sqlite and the mongodb databases that modeldb will use
        # this also starts mongodb
        # ***IMPORTANT NOTE: This clears any previous modeldb databases. This should only be done once.***
        cd [path_to_system]/server/codegen
        ./gen_sqlite.sh
    
        # build and start the server
        cd ..
        ./start_server.sh [thrift_version] &
        # NOTE: if you are building the project in eclipse, you may need to uncomment the pluginManagement tags in pom.xml located in the server directory
    
        # build spark.ml client library
        cd [path_to_system]/engine
        ./build_client.sh
    ```

4. **Run**
    
    **Unsupervised Anomaly Detection**
    ```bash
       spark-submit --class model.workflows.UnsupervisedLearning --master [master] [jar_file_path] --json [pipeline_config] --conf [project_config]
    ```
    **[pipeline_config]** Example in conf/unsupervised_pipeline_example.json 
    **[project_config]** Example in conf/config.properties 
    
    We currently support four effective anomaly detection algorithms including Local Outlier Factor (LOF), KNN, Mahalanobis and Isolation Forest. 
    The parameters for these algorithms can be set directly using the [pipeline_config] json file.
     
    **Example Selectors**
    ```bash
       spark-submit --class selector.workflows.ExampleSelector --master [master] [jar_file_path] --json [pipeline_config] --conf [project_config]
    ```
    **[pipeline_config]** Example in conf/selector_pipeline_example.json
    
    Example selector aims to select **the best** examples to label. Here is how it works. First, the user needs to specify the example sources.  
    The example sources can be objects with top-ranked anomaly scores or one object from each k-means cluster. Then, the user will need to specify the
    example selector applied on the selected sources. The example selector can be identity (output all selected objects) or a smarter selector based on 
    similarities between selected objects and labeled objects. 
    
     
    
