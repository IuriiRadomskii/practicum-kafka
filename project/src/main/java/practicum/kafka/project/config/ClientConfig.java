package practicum.kafka.project.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import practicum.kafka.project.dto.client.ClientRequest;
import practicum.kafka.project.dto.shop.ProductInfo;
import practicum.kafka.project.dto.shop.ProductProjection;
import practicum.kafka.project.serialization.JsonObjectSerializer;
import practicum.kafka.project.serialization.ProductInfoDeserializer;
import practicum.kafka.project.serialization.ProductProjectionDeserializer;
import practicum.kafka.project.services.ClientService;

import java.util.Properties;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class ClientConfig {

    private final ClusterCommonProperties commonProps;

    @Value("${client.user.username}")
    private String user;
    @Value("${client.user.password}")
    private String password;

    private Properties getClientProducerProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, commonProps.getLeaderHost());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2");
        props.put(ProducerConfig.RETRIES_CONFIG, 10);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 500);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 0);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 500);
        props.put(ProducerConfig.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG, 500);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, UUIDSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonObjectSerializer.class.getName());
        props.putAll(commonProps.getSecurityProperties(user, password));
        return props;
    }

    @Bean
    public KafkaProducer<UUID, ClientRequest> clientRequestKafkaProducer() {
        var props = getClientProducerProperties();
        var producer = new KafkaProducer<UUID, ClientRequest>(props);
        Runtime.getRuntime().addShutdownHook(new Thread(producer::close));
        return producer;
    }

    @Bean
    KafkaConsumer<UUID, ProductProjection> clientProductProjectionConsumer() {
        var props = commonProps.getCommonConsumerProperties(user, password);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "client-product-projection");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, UUIDDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ProductProjectionDeserializer.class.getName());
        KafkaConsumer<UUID, ProductProjection> consumer = new KafkaConsumer<>(props);
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::close));
        return consumer;
    }

    @Bean
    KafkaConsumer<UUID, ProductProjection> clientProductProjectionRecommendationsConsumer() {
        var props = commonProps.getCommonConsumerProperties(user, password);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "client-product-projection-recommendations");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, UUIDDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ProductProjectionDeserializer.class.getName());
        KafkaConsumer<UUID, ProductProjection> consumer = new KafkaConsumer<>(props);
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::close));
        return consumer;
    }

    @Bean
    KafkaConsumer<UUID, ProductInfo> dataProductConsumer() {
        var props = commonProps.getCommonConsumerProperties(user, password);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "client-data-product-consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, UUIDDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ProductInfoDeserializer.class.getName());
        KafkaConsumer<UUID, ProductInfo> consumer = new KafkaConsumer<>(props);
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::close));
        return consumer;
    }

    @Bean
    @ConditionalOnProperty(prefix = "client", value = "enabled", havingValue = "true")
    public ClientService clientService(
            @Qualifier("clientRequestKafkaProducer") KafkaProducer<UUID, ClientRequest> clientRequestKafkaProducer,
            @Qualifier("clientProductProjectionConsumer") KafkaConsumer<UUID, ProductProjection> clientProductProjectionConsumer,
            @Qualifier("clientProductProjectionRecommendationsConsumer") KafkaConsumer<UUID, ProductProjection> clientProductProjectionRecommendationsConsumer,
            @Qualifier("dataProductConsumer") KafkaConsumer<UUID, ProductInfo> dataProductConsumer,
            @Value("${client.producer-topic}") String producerTopic,
            @Value("${client.consumer-topic}") String consumerTopic,
            @Value("${client.consumer-recommendation-topic}") String consumerRecommendationTopic,
            @Value("${client.poll-count}") int pollCount,
            @Value("${client.poll-duration}") int pollDuration

    ) {
        return new ClientService(
                clientRequestKafkaProducer,
                clientProductProjectionConsumer,
                clientProductProjectionRecommendationsConsumer,
                dataProductConsumer,
                producerTopic,
                consumerTopic,
                consumerRecommendationTopic,
                pollCount,
                pollDuration
        );
    }

}
