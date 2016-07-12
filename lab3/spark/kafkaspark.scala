import java.util.HashMap

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import kafka.serializer.{DefaultDecoder, StringDecoder}

import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.storage.StorageLevel

object KafkaWordCount {
  def main(args: Array[String]) {

    val kafkaConf = Map(
	"metadata.broker.list" -> "localhost:9092",
	"zookeeper.connect" -> "localhost:2181",
	"group.id" -> "kafka-spark-streaming",
	"zookeeper.connection.timeout.ms" -> "1000")

    val sparkConf = new SparkConf().setAppName("KafkaWordCount").setMaster(<FILL IN>)
    val ssc = new StreamingContext(<FILL IN>)
    ssc.checkpoint("checkpoint")

    // if you want to try the receiver-less approach, comment the below line and uncomment the next one
    val messages = KafkaUtils.createStream[String, String, DefaultDecoder, StringDecoder](<FILL IN>)
    //val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](<FILL IN>)

    val values = messages.map(<FILL IN>)
    val pairs = values.<FILL IN>


    def mappingFunc(key: String, value: Option[Double], state: State[Double]): Option[(String, Double)] = {
	<FILL IN>
    }


    val stateDstream = pairs.mapWithState(<FILL IN>)

    stateDstream.print()
    ssc.start()
    ssc.awaitTermination()
  }
}
