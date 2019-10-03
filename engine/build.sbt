name := "AnomalyDetection Spark Client"

version := "1.0"

scalaVersion := "2.11.8"

test in assembly := {}
assemblyJarName in assembly := "anomalydetection-scala-client.jar"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.4"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.4" % Provided

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.4" % Provided

libraryDependencies += "org.apache.thrift" % "libthrift" % "0.9.3" exclude("org.slf4j", "slf4j-api")

libraryDependencies += "com.twitter" %% "scrooge-core" % "4.12.0" exclude("com.twitter", "libthrift")
libraryDependencies += "com.twitter" %% "finagle-thrift" % "6.36.0" exclude("com.twitter", "libthrift")

libraryDependencies += "com.github.scopt" %% "scopt" % "3.5.0"

libraryDependencies += "commons-configuration" % "commons-configuration" % "1.7"

libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.16" % Test
libraryDependencies += "commons-cli" % "commons-cli" % "1.2"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"
parallelExecution in test := false
