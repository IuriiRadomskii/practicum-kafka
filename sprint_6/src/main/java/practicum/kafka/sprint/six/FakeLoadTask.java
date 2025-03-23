package practicum.kafka.sprint.six;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import practicum.kafka.sprint.six.components.Consumer;
import practicum.kafka.sprint.six.components.SchemaRegistryService;
import practicum.kafka.sprint.six.components.UserProducer;
import practicum.kafka.sprint.six.dto.User;

import java.io.IOException;
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
    private final SchemaRegistryService schemaRegistryService;
    private final SchemaRegistryClient schemaRegistryClient;

    @Value("${number-of-messages}")
    private Integer numberOfMessages;
    @Value("${task-1.topic}")
    private String testTopic;
    @Value("${run-task}")
    private int taskNumber;
    @Value("${task-2.topic-sink}")
    private String sinkTopic;
    @Value("${task-2.topic-source}")
    private String sourceTopic;

    private static User getRandomUser() {
        return new User(
                String.valueOf(RANDOM.nextLong(1000)),
                RANDOM.nextInt(1000),
                COLORS.get(RANDOM.nextInt(0, COLORS.size() - 1))
        );
    }

    @Override
    public void run(String... args) throws RestClientException, IOException {
        if (numberOfMessages < 2) {
            runTask1();
        } else {
            runTask2();
        }
    }

    private void runTask1() throws RestClientException, IOException {
        var subject = schemaRegistryClient.getAllSubjects();
        var versions = schemaRegistryClient.getAllVersions("cluster-topic");
        log.info("Subjects: {}, versions: {}", subject, versions);
        if (subject.isEmpty()) {
            String schema = schemaRegistryService.loadSchema("user_schema.json");
            schemaRegistryService.registerSchema(testTopic, schema);
        }

        final int finalNumberOfMessages = numberOfMessages;
        executorService.submit(() -> {
            for (int i = 0; i < finalNumberOfMessages; i++) {
                userProducer.send(testTopic, getRandomUser());
            }
            executorService.submit(() -> consumer.consume(testTopic));
        });
    }

    private void runTask2() {
        final int finalNumberOfMessages = numberOfMessages;
        executorService.submit(() -> {
            for (int i = 0; i < finalNumberOfMessages; i++) {
                emulateLoad();
                userProducer.send(sourceTopic, getRandomUser());
            }
        });
        executorService.submit(() -> consumer.consume(sinkTopic));
    }

    private static void emulateLoad() {
        try {
            Thread.sleep(RANDOM.nextLong(500, 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Load too high exception");
        }
    }

}
