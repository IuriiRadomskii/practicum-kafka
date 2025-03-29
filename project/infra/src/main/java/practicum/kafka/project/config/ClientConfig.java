package practicum.kafka.project.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import practicum.kafka.project.dto.client.ClientProductSearchRequest;
import practicum.kafka.project.serialization.JsonObjectSerializer;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class ClientConfig {

    private final ClusterCommonProperties commonProps;

    @Value("${client.user.username}")
    private String producerUser;
    @Value("${client.user.password}")
    private String producerPassword;

    private Properties getShopProducerProperties() {
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
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonObjectSerializer.class.getName());
        props.putAll(commonProps.getSecurityProperties(producerUser, producerPassword));
        return props;
    }

    @Bean
    public KafkaProducer<String, ClientProductSearchRequest> clientRequestKafkaProducer() {
        var props = getShopProducerProperties();
        var producer = new KafkaProducer<String, ClientProductSearchRequest>(props);
        Runtime.getRuntime().addShutdownHook(new Thread(producer::close));
        return producer;
    }

}
