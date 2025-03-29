package practicum.kafka.sprint.four.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import practicum.kafka.sprint.four.connector.MetricEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomMetricsProducer {

    private static final int METRICS_COUNT = 300;
    private static final String TOPIC = "metrics-event-topic";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(CustomMetricsProducer.class);

    private static Map<String, Object> commonProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094,localhost:9095,localhost:9096");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 500);
        props.put(ProducerConfig.RETRY_BACKOFF_MAX_MS_CONFIG, 5000);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    public static void main(String[] args) {
        try (var producer = new KafkaProducer<String, Map<String, MetricEvent>>(commonProps())) {
            for (int i = 0; i < METRICS_COUNT; i++) {
                ProducerRecord<String, Map<String, MetricEvent>> record = new ProducerRecord<>(
                        TOPIC,
                        UUID.randomUUID().toString(),
                        Map.of(
                                "Alloc", new MetricEvent(
                                        "Alloc",
                                        "gauge",
                                        "Alloc desc",
                                        "120.0"),
                                "FreeMemory", new MetricEvent(
                                        "FreeMemory",
                                        "gauge",
                                        "FreeMemory desc",
                                        String.valueOf((double) 120 - i)),
                                "TotalMemory", new MetricEvent(
                                        "TotalMemory",
                                        "gauge",
                                        "TotalMemory desc",
                                        String.valueOf((double) i))
                        )
                );
                producer.send(record);
            }
        }
    }

    public static class JsonSerializer implements Serializer<Object> {

        @Override
        public byte[] serialize(String topic, Object data) {
            try {
                return MAPPER.writeValueAsBytes(data);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize data: {}", e.getMessage(), e);
                return new byte[0];
            }
        }

    }

}
