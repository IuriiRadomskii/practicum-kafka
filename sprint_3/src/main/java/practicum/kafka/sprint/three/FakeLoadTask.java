package practicum.kafka.sprint.three;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import practicum.kafka.sprint.three.model.UserBlockEvent;
import practicum.kafka.sprint.three.model.UserMessage;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FakeLoadTask implements CommandLineRunner {

    private final KafkaTemplate<UUID, UserMessage> userMessageTemplate;
    private final KafkaTemplate<UUID, UserBlockEvent> userBlockEventTemplate;
    private final KafkaTemplate<UUID, String> forbiddenWordsTemplate;

    private static UUID uuid() {
        return UUID.randomUUID();
    }

    @Override
    public void run(String... args) throws Exception {
        userMessageTemplate.send("user_messages", uuid(), new UserMessage(uuid(), "FOOBAR"));
        userBlockEventTemplate.send("user_block_events", uuid(), new UserBlockEvent(uuid()));
        forbiddenWordsTemplate.send("forbidden_words", uuid(), "SHIT");
    }

}
