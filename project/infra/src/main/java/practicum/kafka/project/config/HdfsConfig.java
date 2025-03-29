package practicum.kafka.project.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HdfsConfig implements CommandLineRunner {

    private final ClusterCommonProperties commonProps;

    @Value("${hdfs.user.username}")
    private String hdfsUser;
    @Value("${hdfs.user.password}")
    private String hdfsPassword;
    @Value("${hdfs.topics}")
    private List<String> hdfsTopics;
    @Value("${hdfs.url}")
    private String hdfsUrl;
    @Value("${hdfs.file-path}")
    private String filePath;

    @Override
    public void run(String... args) throws Exception {
        //transferToHdfs();
    }

    private void transferToHdfs() {
        var props = commonProps.getCommonConsumerProperties(
                hdfsUser,
                hdfsPassword
        );
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "hdfs-consumer");
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(hdfsTopics);
            org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
            conf.set("fs.defaultFS", hdfsUrl);
            try (FileSystem hdfs = FileSystem.get(new URI(hdfsUrl), conf, "root")) {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                    log.info("Records count: {}", records.count());
                    for (ConsumerRecord<String, String> record : records) {
                        String value = record.value();
                        String hdfsFilePath = String.format(filePath,
                                record.topic().replaceAll("\\.", "_"),
                                record.key() + ".json"
                        );
                        Path path = new Path(hdfsFilePath);
                        try (FSDataOutputStream outputStream = hdfs.create(path, true)) {
                            log.info("Writing {} to {}", value, hdfsFilePath);
                            outputStream.writeUTF(value);
                        }

                    }
                }
            }
        } catch (Exception e) {
            log.error("Error while transferring data to HDFS: {}", e.getMessage(), e);
        }
    }
}

