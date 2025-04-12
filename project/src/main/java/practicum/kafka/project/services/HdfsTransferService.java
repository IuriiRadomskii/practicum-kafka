package practicum.kafka.project.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import practicum.kafka.project.serialization.ClientRequestDeserializer;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
public class HdfsTransferService {

    @Value("${hdfs.disabled}")
    private boolean disabled;
    @Value("${hdfs.topics}")
    private List<String> hdfsTopics;
    @Value("${hdfs.url}")
    private String hdfsUrl;
    @Value("${hdfs.file-path}")
    private String filePath;
    private final Properties props;

    public HdfsTransferService(
            @Qualifier("hdfsConsumerProps") Properties hdfsConsumerProps
    ) {
        this.props = hdfsConsumerProps;
    }

    public void transferDataToHdfs() {
        if (disabled) {
            log.info("Skip hadoop transfer");
            return;
        }
        log.info("Start hdfs transfer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "hdfs-consumer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ClientRequestDeserializer.class.getName());
        org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
        conf.set("fs.defaultFS", hdfsUrl);
        conf.set("dfs.client.socket-timeout", "10000");
        conf.set("dfs.client.use.datanode.hostname", "true");
        try (FileSystem hdfs = FileSystem.get(new URI(hdfsUrl), conf, "root")) {
            log.info("Open hdfs filesystem");
            for (int i = 0; i < 30; i++) {
                Path path = new Path(filePath + i);
                try (FSDataOutputStream out = hdfs.create(path, true)) {
                    log.info("Created file: {}", path.toUri());
                    out.writeUTF("Entry with number " + i);
                }
            }
            log.info("Files added to hdfs data lake");
            System.exit(1);

            /*try (KafkaConsumer<String, ClientRequest> consumer = new KafkaConsumer<>(props)) {
                consumer.subscribe(hdfsTopics);
                while (true) {
                    ConsumerRecords<String, ClientRequest> records = consumer.poll(Duration.ofMillis(4000));
                    log.info("Records count: {}", records.count());
                    for (ConsumerRecord<String, ClientRequest> record : records) {
                        ClientRequest value = record.value();
                        try (FSDataOutputStream out = hdfs.append(path)) {
                            log.info("Writing to file product name: {}", value.getProductName());
                            out.writeUTF(value.getProductName() + "\n");
                        }
                    }
                }
            }*/
        } catch (Exception e) {
            log.error("Error while transferring data to HDFS: {}", e.getMessage(), e);
        }
    }

    private void appendToHdfs(FileSystem hdfs, Path path, String content) throws IOException {
        try (FSDataOutputStream out = hdfs.append(path)) {
            out.writeUTF(content);
            log.info("Append to: {}", path.toUri());
        }
    }

}
