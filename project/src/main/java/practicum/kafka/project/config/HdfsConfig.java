package practicum.kafka.project.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Bean
    public Properties hdfsConsumerProps() {
        return commonProps.getCommonConsumerProperties(
                hdfsUser,
                hdfsPassword
        );
    }

}

