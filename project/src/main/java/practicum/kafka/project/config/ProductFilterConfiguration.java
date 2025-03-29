package practicum.kafka.project.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import practicum.kafka.project.dto.shop.ProductInfo;
import practicum.kafka.project.serialization.JsonObjectSerializer;

import java.util.Properties;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ProductFilterConfiguration {

    private final ClusterCommonProperties commonProps;

    @Value("${product-filter.topics.sink}")
    private String topicSink;
    @Value("${product-filter.topics.source}")
    private String topicSource;
    @Value("${product-filter.topics.forbidden-list}")
    private String forbiddenProductsTopic;
    @Value("${product-filter.user.username}")
    private String username;
    @Value("${product-filter.user.password}")
    private String password;
    @Value("${product-filter.app-id}")
    private String appId;

    @Bean("filterStreamProperties")
    public Properties filterStreamProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, commonProps.getLeaderHost());
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.putAll(commonProps.getSecurityProperties(username, password));
        return props;
    }

    @Bean
    public StreamsBuilder streamBuilder() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KTable<UUID, UUID> forbiddenProducts = streamsBuilder
                .table(forbiddenProductsTopic, Consumed.with(Serdes.UUID(), Serdes.UUID()));

        KStream<UUID, String> products = streamsBuilder
                .stream(topicSource, Consumed.with(Serdes.UUID(), Serdes.String()));

        KStream<UUID, String> allowedProducts = products
                .leftJoin(forbiddenProducts, (productValue, forbiddenValue) -> forbiddenValue == null ? productValue : null)
                .filter((productId, product) -> product != null);

        allowedProducts.to(topicSink, Produced.with(Serdes.UUID(), Serdes.String()));

        return streamsBuilder;
    }

    private Properties forbiddenProductProducerProperties() {
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
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.putAll(commonProps.getSecurityProperties(username, password));
        return props;
    }

    @Bean
    public KafkaProducer<UUID, UUID> forbiddenProductsProducer() {
        var props = forbiddenProductProducerProperties();
        var producer = new KafkaProducer<UUID, UUID>(props);
        Runtime.getRuntime().addShutdownHook(new Thread(producer::close));
        return producer;
    }

}
