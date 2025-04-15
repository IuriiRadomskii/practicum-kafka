package practicum.kafka.project.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import practicum.kafka.project.dto.client.ClientRequest;
import practicum.kafka.project.serialization.ClientRequestDeserializer;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
public class HdfsTransferService {

    private final ObjectMapper mapper;
    @Value("${hdfs.group-id}")
    private String groupId;
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
            @Qualifier("hdfsConsumerProps") Properties hdfsConsumerProps,
            ObjectMapper mapper) {
        this.props = hdfsConsumerProps;
        this.mapper = mapper;
    }

    public void transferDataToHdfs() {
        if (disabled) {
            log.info("Skip hadoop transfer");
            return;
        }
        log.info("Start hdfs transfer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ClientRequestDeserializer.class.getName());
        org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
        conf.set("fs.defaultFS", hdfsUrl);
        conf.set("dfs.client.socket-timeout", "10000");
        conf.set("dfs.client.use.datanode.hostname", "true");
        try (FileSystem hdfs = FileSystem.get(new URI(hdfsUrl), conf, "root")) {
            log.info("Open hdfs filesystem");
            try (KafkaConsumer<String, ClientRequest> consumer = new KafkaConsumer<>(props)) {
                consumer.subscribe(hdfsTopics);
                while (true) {
                    ConsumerRecords<String, ClientRequest> records = consumer.poll(Duration.ofMillis(4000));
                    log.info("Records count: {}", records.count());
                    int i = 0;
                    for (ConsumerRecord<String, ClientRequest> record : records) {
                        ClientRequest value = record.value();
                        Path path = new Path(filePath + i);
                        try (FSDataOutputStream out = hdfs.create(path, true)) {
                            out.writeUTF(mapper.writeValueAsString(value));
                            log.info("ClientRequest: {} added to hdfs file: {}", value, path);
                        }
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error while transferring data to HDFS: {}", e.getMessage(), e);
        }
    }
}
