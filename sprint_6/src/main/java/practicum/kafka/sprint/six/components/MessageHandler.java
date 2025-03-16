package practicum.kafka.sprint.six.components;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.stereotype.Component;
import practicum.kafka.sprint.six.dto.User;
import practicum.kafka.sprint.six.exceptions.MessageNotHandledException;

@Slf4j
@Component
public class MessageHandler {

    public void handle(ConsumerRecords<String, User> records) throws MessageNotHandledException {
        records.forEach(r -> {
            log.info("TOPIC: {}, USER: {}",
                    r.topic(),
                    r.value().toString()
            );
        });

    }

}
