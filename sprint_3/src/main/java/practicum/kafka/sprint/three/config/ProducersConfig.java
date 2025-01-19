package practicum.kafka.sprint.three.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import practicum.kafka.sprint.three.serdes.JsonSerializer;
import practicum.kafka.sprint.three.model.UserBlockEvent;
import practicum.kafka.sprint.three.model.UserMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class ProducersConfig {

    private final KafkaProperties kafkaProperties;

    private Map<String, Object> commonProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 500);
        props.put(ProducerConfig.RETRY_BACKOFF_MAX_MS_CONFIG, 5000);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        return props;
    }

    @Bean
    public KafkaTemplate<UUID, UserMessage> messageTemplate() {
        var props = commonProps();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, UUIDSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        var factory = new DefaultKafkaProducerFactory<UUID, UserMessage>(props);
        var template = new KafkaTemplate<>(factory, props);
        template.setDefaultTopic("user_messages");
        return template;
    }

    @Bean
    public KafkaTemplate<UUID, UserBlockEvent> userBlockEventsTemplate() {
        var props = commonProps();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, UUIDSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        var factory = new DefaultKafkaProducerFactory<UUID, UserBlockEvent>(props);
        var template = new KafkaTemplate<>(factory, props);
        template.setDefaultTopic("user_block_events");
        return template;
    }

    @Bean
    public KafkaTemplate<UUID, String> forbiddenWordsTemplate() {
        var props = commonProps();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, UUIDSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        var factory = new DefaultKafkaProducerFactory<UUID, String>(props);
        var template = new KafkaTemplate<>(factory, props);
        template.setDefaultTopic("forbidden_words");
        return template;
    }

}
