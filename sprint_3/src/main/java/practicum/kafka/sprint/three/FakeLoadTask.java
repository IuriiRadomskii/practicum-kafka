package practicum.kafka.sprint.three;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import practicum.kafka.sprint.three.config.StreamsConfiguration;
import practicum.kafka.sprint.three.model.UserBlockEvent;
import practicum.kafka.sprint.three.model.UserMessage;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FakeLoadTask implements CommandLineRunner {

    private final KafkaTemplate<UUID, UserMessage> userMessageTemplate;
    private final KafkaTemplate<UUID, UserBlockEvent> userBlockEventTemplate;
    private final KafkaTemplate<String, String> forbiddenWordsTemplate;

    private static UUID uuid() {
        return UUID.randomUUID();
    }

    @Override
    public void run(String... args) {
        var sender = uuid();
        var receiver = uuid();

        forbiddenWordsTemplate.send(StreamsConfiguration.FORBIDDEN_WORDS_TOPIC, "FOO", "FOO");
        forbiddenWordsTemplate.send(StreamsConfiguration.FORBIDDEN_WORDS_TOPIC, "BAZ", "BAZ");
        userBlockEventTemplate.send(StreamsConfiguration.USER_BLOCK_EVENTS_TOPIC, receiver, new UserBlockEvent(sender));

        for (int i = 0; i < 100; i++) {
            userMessageTemplate.send(StreamsConfiguration.USER_MESSAGES_TOPIC, sender, new UserMessage(receiver, "FOO BAR BAZ"));
        }
    }
}
