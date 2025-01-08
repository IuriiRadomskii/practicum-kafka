package practicum.kafka.sprint.two.components;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import practicum.kafka.sprint.two.dto.TransactionStatus;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

import static practicum.kafka.sprint.two.config.AppConfig.TOPIC;

@Slf4j
@Component
public class RxConsumer {

    private final Properties props;
    private final MessageHandler messageHandler;

    public RxConsumer(@Qualifier("rxConsumerProperties") Properties props,
                      MessageHandler messageHandler) {
        this.props = props;
        this.messageHandler = messageHandler;
    }

    public void consume() {
        try (KafkaConsumer<String, TransactionStatus> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of(TOPIC));
            log.info("Starting rx consumer...");
            while (true) {
                var records = consumer.poll(Duration.ofMillis(10)); //типа реактивный консюмер
                if (!records.isEmpty()) {
                    try {
                        if (!records.isEmpty()) {
                            log.info("Got {} records for {}", records.count(), props.get(ConsumerConfig.GROUP_ID_CONFIG));
                            messageHandler.handle(records);
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
        }
    }
}
