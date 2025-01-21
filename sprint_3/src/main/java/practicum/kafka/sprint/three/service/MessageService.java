package practicum.kafka.sprint.three.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import practicum.kafka.sprint.three.model.UserMessage;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    public void sendMessage(UUID fromUser, UserMessage userMessage) {

        log.info("Message from {} to {}: {}", fromUser, userMessage.to(), userMessage.content());

    }

}
