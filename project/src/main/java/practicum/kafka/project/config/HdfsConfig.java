package practicum.kafka.project.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HdfsConfig {

    private final ClusterCommonProperties commonProps;

    @Value("${hdfs.user.username}")
    private String hdfsUser;
    @Value("${hdfs.user.password}")
    private String hdfsPassword;
    @Value("${replica.leader-host}")
    private String replicaLeaderHost;

    @Bean
    public Properties hdfsConsumerProps() {
        var replicaConsumer = commonProps.getCommonConsumerProperties(
                hdfsUser,
                hdfsPassword
        );
        //replicaConsumer.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, replicaLeaderHost);
        return replicaConsumer;
    }

}

