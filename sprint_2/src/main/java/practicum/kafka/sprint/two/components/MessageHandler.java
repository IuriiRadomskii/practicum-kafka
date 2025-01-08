package practicum.kafka.sprint.two.components;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.stereotype.Component;
import practicum.kafka.sprint.two.dto.TransactionStatus;
import practicum.kafka.sprint.two.exceptions.MessageNotHandledException;

import java.util.Random;

@Slf4j
@Component
public class MessageHandler {

    private static final Random RANDOM = new Random();

    public void handle(ConsumerRecords<String, TransactionStatus> records) throws MessageNotHandledException {
        if (RANDOM.nextInt(100) > 75) {
            throw new MessageNotHandledException("Exception while handling records");
        }
        records.forEach(r -> {
            log.info("TOPIC: {}, ID: {}, STATUS: {}",
                    r.topic(),
                    r.value().transactionId(),
                    r.value().status()
            );
        });

    }

}
