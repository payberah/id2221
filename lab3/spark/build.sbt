name := "spark_kafka"

version := "1.0"

scalaVersion := "2.10.5"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.6.2",
  "org.apache.spark" % "spark-streaming_2.10" % "1.6.2",
  "org.apache.spark" % "spark-streaming-kafka_2.10" % "1.6.2"
)

