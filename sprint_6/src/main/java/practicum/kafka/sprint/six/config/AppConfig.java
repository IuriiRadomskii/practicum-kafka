package practicum.kafka.sprint.six.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import practicum.kafka.sprint.six.dto.TransactionStatus;
import practicum.kafka.sprint.six.serialization.JsonDeserializer;
import practicum.kafka.sprint.six.serialization.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class AppConfig {

    @Value("${leader.host}")
    private String leaderHost;
    @Value("${trust.store.location}")
    private String trustStoreLocation;
    @Value("${trust.store.password}")
    private String trustStorePassword;
    @Value("${key.store.location}")
    private String keyStoreLocation;
    @Value("${key.store.password}")
    private String keyStorePassword;
    @Value("${ssl.key.password}")
    private String sslKeyPassword;

    private Properties getCommonProducerConfig() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, leaderHost);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2");
        props.put(ProducerConfig.RETRIES_CONFIG, 10); // попытается еще два 10 раз если реплики не аккнут прием сообщения (ack all, sync replicas 2)
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 500); //чтобы не спамить кафку ставим тайм-аут
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        return props;
    }

    private Map<Object, Object> getSaslSslProperties() {
        Map<Object, Object> props = new HashMap<>();
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, trustStoreLocation); // Путь к truststore
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, trustStorePassword); // Пароль truststore
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keyStoreLocation); // Путь к keystore
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keyStorePassword); // Пароль keystore
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, sslKeyPassword);

        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        return props;
    }

    @Bean
    public KafkaProducer<String, TransactionStatus> producerSasl() {
        var props = getCommonProducerConfig();
        props.putAll(getSaslSslProperties());
        props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=producer password=producer-secret;");
        var producer = new KafkaProducer<String, TransactionStatus>(props);
        Runtime.getRuntime().addShutdownHook(new Thread(producer::close));
        return producer;
    }

    @Bean
    public Properties consumerProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, leaderHost);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group-consumer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
        props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, "1000");
        props.putAll(getSaslSslProperties());
        props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=consumer password=consumer-secret;");

        return props;
    }

}
