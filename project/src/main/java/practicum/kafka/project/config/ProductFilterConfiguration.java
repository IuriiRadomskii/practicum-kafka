package practicum.kafka.project.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import practicum.kafka.project.dto.shop.ProductInfo;
import practicum.kafka.project.serialization.CustomSerdes;
import practicum.kafka.project.serialization.ProductInfoSerde;

import java.util.List;
import java.util.Properties;
import java.util.Set;
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
    @Value("${product-filter.user.username}")
    private String username;
    @Value("${product-filter.user.password}")
    private String password;
    @Value("${product-filter.app-id}")
    private String appId;
    @Value("${product-filter.topics.filter-names-topic}")
    private String filterNamesTopic;
    @Value("${product-filter.filter-names-store}")
    private String filterNamesStore;

    @Bean("filterStreamProperties")
    public Properties filterStreamProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, commonProps.getLeaderHost());
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.UUIDSerde.class.getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, ProductInfoSerde.class.getName());
        props.putAll(commonProps.getSecurityProperties(username, password));
        return props;
    }

    @Bean
    public StreamsBuilder productInfoStreamBuilder() {
        StreamsBuilder builder = new StreamsBuilder();

        StoreBuilder<KeyValueStore<String, Set<String>>> namesStore =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(filterNamesStore),
                        Serdes.String(),
                        CustomSerdes.setSerde()
                );

        builder.addStateStore(namesStore);
        //add forbidden names prefixes
        builder.stream(filterNamesTopic, Consumed.with(Serdes.String(), Serdes.String()))
                .process(() -> new FilterNamesStoreProcessor(filterNamesStore), filterNamesStore);

        KStream<UUID, ProductInfo> products = builder
                .stream(topicSource, Consumed.with(Serdes.UUID(), new ProductInfoSerde()));

        //filtering products by name prefix
        products.process(() -> new ProductFilterProcessor(filterNamesStore), filterNamesStore)
                .to(topicSink);

        return builder;
    }

    //@Bean
    public KafkaProducer<String, String> namesProducer() {
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
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.putAll(commonProps.getSecurityProperties(username, password));
        var producer = new KafkaProducer<String, String>(props);
        Runtime.getRuntime().addShutdownHook(new Thread(producer::close));
        return producer;
    }

}
