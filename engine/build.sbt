name := "AnomalyDetection Spark Client"

version := "1.0"

scalaVersion := "2.11.8"

test in assembly := {}
assemblyJarName in assembly := "anomalydetection-scala-client.jar"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.4"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.3.3" % "provided"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.3.3" % "provided"

libraryDependencies += "org.apache.thrift" % "libthrift" % "0.9.3" exclude("org.slf4j", "slf4j-api")
libraryDependencies += "com.twitter" %% "scrooge-core" % "4.12.0" exclude("com.twitter", "libthrift")
libraryDependencies += "com.twitter" %% "finagle-thrift" % "6.36.0" exclude("com.twitter", "libthrift")

libraryDependencies += "com.github.scopt" %% "scopt" % "3.5.0"
