package practicum.kafka.sprint.five.components;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import practicum.kafka.sprint.five.dto.TransactionStatus;
import practicum.kafka.sprint.five.exceptions.MessageNotHandledException;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

import static practicum.kafka.sprint.five.config.AppConfig.TOPIC_1;
import static practicum.kafka.sprint.five.config.AppConfig.TOPIC_2;

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

    public void consume() {
        try (KafkaConsumer<String, TransactionStatus> consumer = new KafkaConsumer<>(props)) {
            log.info("Starting consumer...");
            consumer.subscribe(List.of(TOPIC_1, TOPIC_2));
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
