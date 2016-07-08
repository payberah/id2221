![hadoop Logo](/files/logo/hadoop.png)
# **Lab 1 - Hadoop MapReduce and HDFS**
#### The following steps (Part 1 and Part 2) demonstrate how to install HDFS and create and run "word count" application with Hadoop MapReduce. Then, in Part 3, you are asked to implement ... with Hadoop MapReduce.

## Part 1: HDFS

### Install HDFS

1. Download the Hadoop platform from the following link:
[Hadoop](http://www.apache.org/dyn/closer.cgi/hadoop/common/hadoop-2.6.4/hadoop-2.6.4.tar.gz)

2. Set the environment variable.
```bash
export JAVA_HOME="<JAVA PATH>"
export HADOOP_HOME="<HADOOP PATH>/hadoop-2.6.4"
export HADOOP_CONFIG="$HADOOP_HOME/etc/hadoop"
```

3. Specify environment variables in `$HADOOP_CONFIG/hadoop-env.sh`.
```bash
export JAVA_HOME="<JAVA PATH>"
```

4. Make three folders on local file system, where HDFS namenode and datanode store their data.
```bash
mkdir -p $HADOOP_HOME/hdfs/namenode
mkdir -p $HADOOP_HOME/hdfs/datanode
```

5. The main HDFS configuration file is located at `$HADOOP_CONFIG/hdfs-site.xml`. Specify the folders path, built in step 4.
```xml
<configuration>
  <property>
    <name>dfs.namenode.name.dir</name>
    <value>file:///<HADOOP HOME PATH>/hdfs/namenode</value>
    <description>Path on the local filesystem where the NameNode stores the namespace and transaction logs persistently.</description>
  </property>

  <property>
    <name>dfs.datanode.data.dir</name>
    <value>file:///<HADOOP HOME PATH>/hdfs/datanode</value>
    <description>Comma separated list of paths on the local filesystem of a DataNode where it should store its blocks.</description>
  </property>
</configuration>
```

6. Specify the address of the namenode (master) in `$HADOOP_CONFIG/core-site.sh`.
```xml
<configuration>
  <property>
    <name>fs.defaultFS</name>
    <value>hdfs://127.0.0.1:9000</value>
    <description>NameNode URI</description>
  </property>
</configuration>
```

7. Format the namenode directory (DO THIS ONLY ONCE, THE FIRST TIME).
```bash
$HADOOP_HOME/bin/hdfs namenode -format
```

8. Start the namenode and datanode daemons
```bash
$HADOOP_HOME/sbin/hadoop-daemon.sh start namenode
$HADOOP_HOME/sbin/hadoop-daemon.sh start datanode
```

### Test HDFS
1. Prints out the HDFS running processes, by running the `jps` command in a terminal.
2. Monitor the process through their web interfaces:
   - Namenode: [http://127.0.0.1:50070](http://127.0.0.1:50070)
   - Datanode: [http://127.0.0.1:50075](http://127.0.0.1:50075)
3. Try HDFS commands
```bash
# Create a new directory /sics on HDFS
$HADOOP_HOME/bin/hdfs dfs -mkdir /sics

# Create a file, call it big, on your local filesystem and upload it to HDFS under /sics.
$HADOOP_HOME/bin/hdfs dfs -put big /sics

# View the content of /sics directory
$HADOOP_HOME/bin/hdfs dfs -ls big /sics

# Determine the size of big on HDFS
$HADOOP_HOME/bin/hdfs dfs -du -h /sics/big

# Print the first 5 lines to screen from big on HDFS
$HADOOP_HOME/bin/hdfs dfs -cat /sics/big | head -n 5
   
# Copy big to /big hdfscopy on HDFS
$HADOOP_HOME/bin/hdfs dfs -cp /sics/big /sics/big_hdfscopy
 
# Copy big back to local filesystem and name it big localcopy
$HADOOP_HOME/bin/hdfs dfs -get /sics/big big_localcopy
   
# Check the entire HDFS filesystem for inconsistencies/problems
$HADOOP_HOME/bin/hdfs fsck /
   
# Delete big from HDFS
$HADOOP_HOME/bin/hdfs dfs -rm /sics/big
   
# Delete /sics directory from HDFS
$HADOOP_HOME/bin/hdfs dfs -rm -r /sics
```

## ** Part 2: MapReduce **

### Simple Word Count
WordCount is a simple application that counts the number of occurrences of each word in a given input set. Below we will take a look at mapper and reducer in detail and then we present the complete code and show you how to compile and run the code.

#### Word Count Mapper
```java
public static class Map extends MapReduceBase 
    implements Mapper<LongWritable, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(LongWritable key, Text value, 
        OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
        String line = value.toString();
        StringTokenizer tokenizer = new StringTokenizer(line);
        while (tokenizer.hasMoreTokens()) {
            word.set(tokenizer.nextToken());
            output.collect(word, one);
        }
    }
}
```

The `Mapper<LongWritable, Text, Text, IntWritable>` refers to the data type of input and output key-value pairs specific to the mapper or rateher the map method, i.e., `Mapper<Input Key Type, Input Value Type, Output Key Type, Output Value Type>`. In our example, the input to a mapper is a single line, so this Text forms the input value. The input key would a long value assigned in default based on the position of Text in input file. Our output from the mapper is of the format "(Word, 1)" hence the data type of our output key value pair is `<Text(String),  IntWritable(int)>`.

In the `map` method, the first and second parameter refer to the data type of the input Key and Value to the mapper. The third parameter is the output collector that does the job of taking the output data. With the output collector we need to specify the data types of the output Key and Value from the mapper. The fourth parameter is used to report the task status internally in Hadoop environment to avoid time outs.

The functionality of the map method is as follows:
1. Create a IntWritable variable ‘one’ with value as 1
2. Convert the input line in Text type to a String
3. Use a tokenizer to split the line into words
4. Iterate through each word and a form key value pairs as
    1. Assign each work from the tokenizer(of String type) to a Text 'word'
    2. Form key value pairs for each word as <word,one> and push it to the output collector
    
#### Word Count Mapper
