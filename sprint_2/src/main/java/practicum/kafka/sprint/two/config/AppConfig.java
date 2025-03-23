package practicum.kafka.sprint.two.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import practicum.kafka.sprint.two.dto.TransactionStatus;
import practicum.kafka.sprint.two.serialization.JsonDeserializer;
import practicum.kafka.sprint.two.serialization.JsonSerializer;

import java.util.Properties;

@Configuration
public class AppConfig {

    public static final String TOPIC = "trx_statuses";

    @Value("${leader.host:kafka-0:9092,kafka-1:9092,kafka-2:9092}")
    private String leaderHost;

    @Bean
    public KafkaProducer<String, TransactionStatus> producer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, leaderHost);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2");
        props.put(ProducerConfig.RETRIES_CONFIG, 10); // попытается еще два 10 раз если реплики не аккнут прием сообщения (ack all, sync replicas 2)
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 500); //чтобы не спамить кафку ставим тайм-аут
        props.put(ProducerConfig.RETRY_BACKOFF_MAX_MS_CONFIG, 5000); //если кафка отвалилась, то время тайм-аута вырастает до этого значения
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        var producer = new KafkaProducer<String, TransactionStatus>(props);
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
        return props;
    }

    @Bean
    public Properties rxConsumerProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, leaderHost);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "rx-consumer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); //возможно? такой консюмер нужен для сбора событий, события до подключения могут и протухнуть
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1); // непонятно как кэш консюмера раздует хип в таком случае
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        return props;
    }


}
