package practicum.kafka.sprint.six;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import practicum.kafka.sprint.six.components.Consumer;
import practicum.kafka.sprint.six.components.UserProducer;
import practicum.kafka.sprint.six.dto.User;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FakeLoadTask implements CommandLineRunner {

    private static final Random RANDOM = new Random();
    private static final List<String> COLORS = List.of("YELLOW", "BLUE", "CYAN", "GREEN", "PINK", "ORANGE", "RED", "WHITE");

    private final UserProducer userProducer;
    private final Consumer consumer;
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    @Value("${number-of-messages}")
    private Integer numberOfMessages;
    @Value("${task-1.topic}")
    private String testTopic;

    private static User getRandomUser() {
        return new User(
                String.valueOf(RANDOM.nextLong(1000)),
                RANDOM.nextInt(1000),
                COLORS.get(RANDOM.nextInt(0, COLORS.size() - 1))
        );
    }

    private static void emulateLoad() {
        try {
            Thread.sleep(RANDOM.nextLong(50, 200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Load too high exception");
        }
    }

    @Override
    public void run(String... args) {

        final int finalNumberOfMessages = numberOfMessages;
        executorService.submit(() -> {
            for (int i = 0; i < finalNumberOfMessages; i++) {
                emulateLoad();
                userProducer.send(testTopic, getRandomUser());
            }
            executorService.submit(() -> consumer.consume(testTopic));
        });
    }

}
