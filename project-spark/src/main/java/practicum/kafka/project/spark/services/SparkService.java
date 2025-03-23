package practicum.kafka.project.spark.services;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SparkService {

    static Logger log = LoggerFactory.getLogger(SparkService.class);

    private static String HDFS_FILE_PATH = "hdfs://localhost:9000/data/client_requests.txt";

    public static void main(String[] args) {
        try {
            processHdfsFile();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void processHdfsFile() {
        log.info("Spark processing HDFS file started");
        SparkConf sparkConf = new SparkConf()
                .setAppName("KafkaHdfsSparkConsumer")
                .setMaster("spark://localhost:7077");

        try (JavaSparkContext sc = new JavaSparkContext(sparkConf)) {
            JavaRDD<String> hdfsData = sc.textFile(HDFS_FILE_PATH);
            long count = hdfsData.count();
            log.info("HDFS file entries count: {}", count);
            List<String> data = new ArrayList<>(hdfsData.collect());
            data.forEach(line -> log.info("Some cool business logic happened here with line: {}", line));
        }
        log.info("Spark processing HDFS file completed");
    }

}
