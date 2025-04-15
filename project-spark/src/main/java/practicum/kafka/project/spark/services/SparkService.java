package practicum.kafka.project.spark.services;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaFutureAction;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class SparkService {

    static Logger log = LoggerFactory.getLogger(SparkService.class);

    private static String SPARK = "spark://localhost:7077";
    private static String HDFS = "hdfs://hadoop-namenode:9000";
    private static String HDFS_FILE_PATH = HDFS + "/data/*";

    public static void main(String[] args) {
        try {
            processHdfsFile();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void processHdfsFile() {
        log.info("Spark processing HDFS file started");
        SparkConf sparkConf =
                new SparkConf()
                        .setAppName("spark-application")
                        .setMaster(SPARK)
                        .set("spark.hadoop.fs.default.name", HDFS)
                        .set("spark.hadoop.fs.defaultFS", HDFS)
                        .set("spark.hadoop.fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName())
                        .set("spark.hadoop.fs.hdfs.server", org.apache.hadoop.hdfs.server.namenode.NameNode.class.getName())
                        .set("spark.hadoop.conf", org.apache.hadoop.hdfs.HdfsConfiguration.class.getName())
                        .set("spark.executor.instances", "2")
                        .set("spark.executor.cores", "1")
                        .set("spark.executor.memory", "1g")
                        .set("spark.driver.memory", "1g");

        //Just cannot run on win 11, meh...
        try (JavaSparkContext sc = new JavaSparkContext(sparkConf)) {
            JavaRDD<String> hdfsData = sc.textFile(HDFS_FILE_PATH);
            int id = hdfsData.id();
            log.info("IDDQD ID: {}", id);
            long count = hdfsData.count();
            log.info("IDDQD Count: {}", count);
        } catch (Exception e) {
            log.error("Exception while processing HDFS file", e);
            System.exit(1);
        }
        log.info("Spark processing HDFS file completed");
    }

}
