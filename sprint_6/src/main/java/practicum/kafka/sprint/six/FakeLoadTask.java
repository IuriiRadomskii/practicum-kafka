package practicum.kafka.sprint.six;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import practicum.kafka.sprint.six.components.Consumer;
import practicum.kafka.sprint.six.components.SchemaRegistryHelper;
import practicum.kafka.sprint.six.components.TransactionStatusProducer;
import practicum.kafka.sprint.six.dto.User;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FakeLoadTask implements CommandLineRunner {

    private static final Random RANDOM = new Random();
    private static final List<String> COLOURS = List.of("SUCCESS", "FAILURE", "PENDING");

    private final TransactionStatusProducer transactionStatusProducer;
    private final Consumer consumer;
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final SchemaRegistryHelper schemaRegistryHelper;

    @Value("${number.of.messages}")
    private Integer numberOfMessages;
    @Value("${topic}")
    private String topic;

    private static User getRandomUser() {
        return new User(UUID.randomUUID().toString(), RANDOM.nextInt(100), COLOURS.get(RANDOM.nextInt(COLOURS.size())));
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
        var schema = schemaRegistryHelper.loadSchema("user_schema.json");
        schemaRegistryHelper.registerSchema(topic, schema);
        executorService.submit(() -> {
            for (int i = 0; i < finalNumberOfMessages; i++) {
                emulateLoad();
                transactionStatusProducer.send(topic, getRandomUser());
            }
            //executorService.submit(() -> consumer.consume(topic));
        });
    }

}
