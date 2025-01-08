package practicum.kafka.sprint.two.examples;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;

public class ReplicationConfigExample {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        ProducerRecord<String, String> record = new ProducerRecord<>("my-topic", "sync", "syncval");
        send("all", record);
        long endTime = System.currentTimeMillis();
        System.out.println("Total time taken: " + (endTime - startTime) + " ms"); //1031ms!!

        startTime = System.currentTimeMillis();
        record = new ProducerRecord<>("my-topic", "sync", "syncval");
        send("1", record);
        endTime = System.currentTimeMillis();
        System.out.println("Total time taken: " + (endTime - startTime) + " ms"); //43ms

    }

    static void send(String ackConfig, ProducerRecord<String, String> record) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094");
        props.put(ProducerConfig.ACKS_CONFIG, ackConfig);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2"); // Минимум 2 реплики должны подтвердить запись
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        record.headers().add(new RecordHeader("X-TRACE-ID", UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)));
        producer.send(record);
        producer.close();
    }

}