package practicum.kafka.project.spark.services;

import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaFutureAction;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SparkService {

    static Logger log = LoggerFactory.getLogger(SparkService.class);

    private static String SPARK = "spark://localhost:7077";
    private static String HDFS = "hdfs://localhost:9000";
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
                        .set("spark.executor.instances", "10")
                        .set("spark.executor.cores", "1")
                        .set("spark.executor.memory", "1g")
                        .set("spark.driver.memory", "1g");

        try (JavaSparkContext sc = new JavaSparkContext(sparkConf)) {
            JavaRDD<String> hdfsData = sc.textFile(HDFS_FILE_PATH);
            JavaFutureAction<Long> f = hdfsData.countAsync();
            while (!f.isDone()) {
                log.info("Not done yet");
            }
            f.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("Spark processing HDFS file completed");
    }

}
