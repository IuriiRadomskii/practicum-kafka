package practicum.kafka.sprint.six.config;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import practicum.kafka.sprint.six.dto.User;
import practicum.kafka.sprint.six.serialization.JsonDeserializer;
import practicum.kafka.sprint.six.serialization.JsonSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class AppConfig {

    @Value("${leader.host}")
    private String leaderHost;
    @Value("${task-1.cacerts.location}")
    private String cacertsLocation;
    @Value("${task-1.cacerts.password}")
    private String cacertsPassword;
    @Value("${task-1.producer.user}")
    private String producerUser;
    @Value("${task-1.producer.password}")
    private String producerPassword;
    @Value("${task-1.consumer.user}")
    private String consumerUser;
    @Value("${task-1.consumer.password}")
    private String consumerPassword;
    @Value("${jaas-template}")
    private String jaasTemplate;

    private Properties getCommonProducerConfig() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, leaderHost);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2");
        props.put(ProducerConfig.RETRIES_CONFIG, 10); // попытается еще два 10 раз если реплики не аккнут прием сообщения (ack all, sync replicas 2)
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 500); //чтобы не спамить кафку ставим тайм-аут
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 0);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 500);
        props.put(ProducerConfig.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG, 500);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "SCRAM-SHA-512");
        props.put("sasl.jaas.config", String.format(jaasTemplate, producerUser, producerPassword));
        props.put("ssl.truststore.location", cacertsLocation);
        props.put("ssl.truststore.password", cacertsPassword);
        return props;
    }

    @Bean
    public KafkaProducer<String, User> producer() {
        var props = getCommonProducerConfig();
        var producer = new KafkaProducer<String, User>(props);
        Runtime.getRuntime().addShutdownHook(new Thread(producer::close));
        return producer;
    }

    @Bean
    public Properties consumerProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, leaderHost);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
        props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, "1000");
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "SCRAM-SHA-512");
        props.put("sasl.jaas.config", String.format(jaasTemplate, consumerUser, consumerPassword));
        props.put("ssl.truststore.location", cacertsLocation);
        props.put("ssl.truststore.password", cacertsPassword);
        return props;
    }

    @Bean
    public SchemaRegistryClient schemaRegistryClient(
            @Value("${schema-registry.url}") String schemaRegistryUrl,
            @Value("${schema-registry.user}") String srUser,
            @Value("${schema-registry.password}") String srPassword
    ) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString((srUser + ":" + srPassword).getBytes(StandardCharsets.UTF_8)));
        return new CachedSchemaRegistryClient(
                schemaRegistryUrl,
                10,
                Collections.emptyMap(),
                headers
        );
    }

}
