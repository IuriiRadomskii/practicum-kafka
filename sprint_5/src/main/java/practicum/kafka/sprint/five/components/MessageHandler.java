package practicum.kafka.sprint.five.components;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.stereotype.Component;
import practicum.kafka.sprint.five.dto.TransactionStatus;
import practicum.kafka.sprint.five.exceptions.MessageNotHandledException;

@Slf4j
@Component
public class MessageHandler {

    public void handle(ConsumerRecords<String, TransactionStatus> records) throws MessageNotHandledException {
        records.forEach(r -> {
            log.info("TOPIC: {}, ID: {}, STATUS: {}",
                    r.topic(),
                    r.value().transactionId(),
                    r.value().status()
            );
        });

    }

}
