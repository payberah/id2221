<p align="center"><img src="logo/spark.png" alt="Hadoop Logo" width="250"/></p>
# **Lab 3 - Spark Streaming**
The following steps demonstrate how to create a simple Spark streaming application. In this notebook you will see how to read data stream from Kafka, make a base DStream, and appy functions to it. In this assignment, we will go through the code step by step, but you will need to complete the code as a standalone application.

Spark Streaming is an extension of the core Spark API that enables scalable, high-throughput, fault-tolerant stream processing of live data streams. Spark Streaming provides a high-level abstraction called *discretized stream* or `DStream` that represents a continuous stream of data. `DStream`s can be created either from input data streams from sources such as Kafka, Flume, and Kinesis, or by applying high-level operations on other `DStream`s. Internally, a `DStream` is represented as a sequence of RDDs.

Here, we use Kafka as our datasource, which is a publish-subscribe messaging rethought as a distributed, partitioned, replicated commit log service. The data in this assignment are key-value pairs in the form of "String,int", and we  want to measure the average value of each key and continuously update it, while new pairs arrive. We first need to download and install Kafka, and run its servers. You can download Kafka from [here](https://www.apache.org/dyn/closer.cgi?path=/kafka/0.10.0.0/kafka_2.11-0.10.0.0.tgz).

Kafka uses ZooKeeper to maintain the configuration information, so you need to first start a ZooKeeper server if you do not already have one.
```bash
bin/zookeeper-server-start.sh config/zookeeper.properties
```
Start the Kafka server.
```bash
bin/kafka-server-start.sh config/server.properties
```
Then, create a topic. A topic is a category or feed name to which messages are published. For each topic, the Kafka cluster maintains a partitioned log that looks like this. Let's create a topic named "avg" with a single partition and only one replica.
```bash
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic avg
```
To see the list of topics you can run the following command.
```bash
bin/kafka-topics.sh --list --zookeeper localhost:2181
```
To generate pairs of "String,int" and feed them to Kafka, you are given a code in the `generator` folder. You just need to download the code, go to its folder and run the following command:
```bash
sbt run
```
Now, we can start to implement our code. First, we import the names of the Spark Streaming classes and some implicit conversions from `StreamingContext` into our environment. `StreamingContext` is the main entry point for all streaming functionality. We create a local `StreamingContext` with two execution threads, and a batch interval of 2 second.
```scala
import java.util.HashMap

import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.storage.StorageLevel

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import kafka.serializer.{DefaultDecoder, StringDecoder}

// Create a local StreamingContext with two working thread and batch interval of 2 second
val sparkConf = new SparkConf().setAppName("KafkaStreamAvg").setMaster(<FILL_IN>)
val ssc = new StreamingContext(<FILL_IN>)
```
Saving of the generated RDDs to reliable storage (data checkpointing) is necessary in some stateful transformations that combine data across multiple batches. In such transformations, the generated RDDs depend on RDDs of previous batches, which causes the length of the dependency chain to keep increasing with time. To avoid such unbounded increases in recovery time, intermediate RDDs of stateful transformations are periodically checkpointed to reliable storage (e.g., HDFS) to cut off the dependency chains. Moreover, checkpointing must be enabled for applications with stateful transformations, e.g., mapWithState. You can enable it by setting a directory in a fault-tolerant, reliable file system to which the checkpoint information will be saved. This is done by using `streamingContext.checkpoint(checkpointDirectory)`. This will allow you to use the aforementioned stateful transformations. 
```scala
ssc.checkpoint("checkpoint")
```
Spark Streaming provides two categories of built-in streaming sources: (i) basic sources, which are directly available in the `StreamingContext` API, e.g., file systems, and socket connections, and (ii) advanced sources, which are available through extra utility classes, e.g., Kafka, Flume, Kinesis, and Twitter. Here we receive data from Kafka. There are two approaches to this: (i) using receivers and Kafka’s high-level API, and (ii) without using receivers.

In the receiver-based approach, the data received from Kafka through a receiver is stored in Spark executors, and then jobs launched by Spark Streaming processes the data. However, under default configuration, this approach can lose data under failures. To ensure zero-data loss, then, you have to additionally enable Write Ahead Logs (WAL) in Spark Streaming that synchronously saves all the received Kafka data into a distributed file system. To use this approach, you need to connect to Kafka through `KafkaUtils.createStream`.

The receiver-less "direct" approach, instead of using receivers to receive data, periodically queries Kafka for the latest offsets in each topic+partition, and accordingly defines the offset ranges to process in each batch. When the jobs to process the data are launched, Kafka's simple consumer API is used to read the defined ranges of offsets from Kafka (similar to read files from a file system). In this apporach, you can connect to Kafka by calling `KafkaUtils.createDirectStream`.

In both models, you need to define the Kafka parameteres, as a `Map` of configuration parameters to their values. The list of parameteres are available [here](http://kafka.apache.org/08/configuration.html).
```scala
val kafkaConf = Map(
    "metadata.broker.list" -> "localhost:9092",
    "zookeeper.connect" -> "localhost:2181",
    "group.id" -> "kafka-spark-streaming",
    "zookeeper.connection.timeout.ms" -> "1000")
```

Message are read from Kafka as key and values. In this application we do not make any use of the key. To be able to easily split the message into individual part we use a String type and StringDecoder for the value of the message. The value part of the messages are strings of "String,int", so, you need to split it by "," and make pairs of `(String, int)`.

```scala
val messages = KafkaUtils.createStream[String, String, StringDecoder, StringDecoder](<FILL_IN>)
//or
val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](<FILL_IN>)

val values = messages.map(<FILL_IN>)
val pairs = values.<FILL_IN>
```

Then, you need to measure the average of the values for receiving pairs `(String, int)`, and update the average continuesly as you receive new paris. You can use `mapWithState` with a function as its input to sum of the values for each `String` key and calculate the average.

```scala
def mappingFunc(key: String, value: Option[Double], state: State[Double]): Option[(String, Double)] = {
    <FILL_IN>
}

val stateDstream = pairs.mapWithState(<FILL_IN>)
```
Note that when these lines are executed, Spark Streaming only sets up the computation it will perform when it is started, and no real processing has started yet. To start the processing after all the transformations have been setup, you need to start the computation.
```scala
stateDstream.print()
ssc.start()
ssc.awaitTermination()
```


