package practicum.kafka.sprint.five;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import practicum.kafka.sprint.five.components.Consumer;
import practicum.kafka.sprint.five.components.TransactionStatusProducer;
import practicum.kafka.sprint.five.dto.TransactionStatus;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static practicum.kafka.sprint.five.config.AppConfig.TOPIC_1;
import static practicum.kafka.sprint.five.config.AppConfig.TOPIC_2;

@Slf4j
@Component
@RequiredArgsConstructor
public class FakeLoadTask implements CommandLineRunner {

    private static final Random RANDOM = new Random();
    private static final List<String> STATUSES = List.of("SUCCESS", "FAILURE", "PENDING");

    private final TransactionStatusProducer transactionStatusProducer;
    private final Consumer consumer;
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
                transactionStatusProducer.send(TOPIC_1, getRandomTransactionStatus());
                transactionStatusProducer.send(TOPIC_2, getRandomTransactionStatus());
            }
            //System.exit(1);
        });
        executorService.submit(consumer::consume);
    }

}
