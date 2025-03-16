package practicum.kafka.sprint.six.components;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import practicum.kafka.sprint.six.dto.TransactionStatus;
import practicum.kafka.sprint.six.exceptions.MessageNotHandledException;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
public class Consumer {

    private final Properties props;
    private final MessageHandler messageHandler;

    public Consumer(@Qualifier("consumerProperties") Properties props,
                    MessageHandler messageHandler) {
        this.props = props;
        this.messageHandler = messageHandler;
    }

    public void consume(String topicName) {
        try (KafkaConsumer<String, TransactionStatus> consumer = new KafkaConsumer<>(props)) {
            log.info("Starting consumer...");
            consumer.subscribe(List.of(topicName));
            while (true) {
                log.info("Polling ...");
                var records = consumer.poll(Duration.ofMillis(4000));
                log.info("Polled records: {}", records);
                try {
                    if (!records.isEmpty()) {
                        log.info("Got {} records for {}, partitions: {}",
                                records.count(),
                                props.get(ConsumerConfig.GROUP_ID_CONFIG),
                                records.partitions());
                        messageHandler.handle(records);
                        consumer.commitSync();
                    }
                } catch (MessageNotHandledException e) {
                    log.error("Unable to consume message: {}", e.getMessage(), e);
                }
            }
        }
    }
}
