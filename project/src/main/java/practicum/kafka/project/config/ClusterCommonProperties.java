package practicum.kafka.project.config;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class ClusterCommonProperties {

    @Getter
    @Value("${cluster.leader-host}")
    private String leaderHost;
    @Value("${cluster.security.cacerts.location}")
    private String cacertsLocation;
    @Value("${cluster.security.cacerts.password}")
    private String cacertsPassword;
    @Value("${cluster.security.jaas-template}")
    private String jaasTemplate;
    @Value("${cluster.security.enabled}")
    private boolean securityOn;

    public Properties getSecurityProperties(String username, String password) {
        Properties props = new Properties();
        if (securityOn) {
            props.put("security.protocol", "SASL_SSL");
            props.put("sasl.mechanism", "SCRAM-SHA-512");
            props.put("sasl.jaas.config", String.format(jaasTemplate, username, password));
            props.put("ssl.truststore.location", cacertsLocation);
            props.put("ssl.truststore.password", cacertsPassword);
        }
        return props;
    }

    public Properties getCommonConsumerProperties(String user, String password) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getLeaderHost());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.putAll(getSecurityProperties(user, password));
        return props;
    }

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
