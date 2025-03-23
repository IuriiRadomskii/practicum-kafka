package practicum.kafka.sprint.two;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import practicum.kafka.sprint.two.components.Consumer;
import practicum.kafka.sprint.two.components.RxConsumer;
import practicum.kafka.sprint.two.components.TransactionStatusProducer;
import practicum.kafka.sprint.two.dto.TransactionStatus;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static practicum.kafka.sprint.two.config.AppConfig.TOPIC;

@Slf4j
@Component
@RequiredArgsConstructor
public class FakeLoadTask implements CommandLineRunner {

    private static final Random RANDOM = new Random();
    private static final List<String> STATUSES = List.of("SUCCESS", "FAILURE", "PENDING");

    private final TransactionStatusProducer transactionStatusProducer;
    private final Consumer consumer;
    private final RxConsumer rxConsumer;
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    @Value("${number.of.messages}")
    private Integer numberOfMessages;

    private static TransactionStatus getRandomTransactionStatus() {
        return new TransactionStatus(UUID.randomUUID(), STATUSES.get(RANDOM.nextInt(STATUSES.size())));
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
                transactionStatusProducer.send(TOPIC, getRandomTransactionStatus());
            }
        });
        executorService.submit(consumer::consume);
        executorService.submit(rxConsumer::consume);
    }
}
