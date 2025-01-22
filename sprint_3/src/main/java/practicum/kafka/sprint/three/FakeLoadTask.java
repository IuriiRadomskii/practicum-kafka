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
        var sender = UUID.fromString("f1a1111a-3702-45e5-bf03-20c47375cbf3");
        var receiver1 = UUID.fromString("f2b2222b-3702-45e5-bf03-20c47375cbf3");
        var receiver2 = UUID.fromString("f3c3333c-3702-45e5-bf03-20c47375cbf3");

        forbiddenWordsTemplate.send(StreamsConfiguration.FORBIDDEN_WORDS_TOPIC, "FOO", "FOO");
        forbiddenWordsTemplate.send(StreamsConfiguration.FORBIDDEN_WORDS_TOPIC, "BAZ", "BAZ");
        userBlockEventTemplate.send(StreamsConfiguration.USER_BLOCK_EVENTS_TOPIC, receiver1, new UserBlockEvent(sender));

        for (int i = 0; i < 500; i++) {
            userMessageTemplate.send(StreamsConfiguration.USER_MESSAGES_TOPIC, sender, new UserMessage(receiver1, "FOO BAR BAR"));
            userMessageTemplate.send(StreamsConfiguration.USER_MESSAGES_TOPIC, sender, new UserMessage(receiver2, "BAR BAR BAZ"));
        }
    }
}
