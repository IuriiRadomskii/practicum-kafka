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
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    @Value("${wait.for.kafka}")
    private Integer waitTime;
    @Value("${number.of.messages}")
    private Integer numberOfMessages;
    @Value("${leader.host}")
    private String leaderHost;

    private static TransactionStatus getRandomTransactionStatus() {
        return new TransactionStatus(UUID.randomUUID(), STATUSES.get(RANDOM.nextInt(STATUSES.size())));
    }

    private void waitForKafkaToBeUpAndRunning(Integer waitTime) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, leaderHost);
        Admin admin = null;
        try {
            for (int i = 0; i < waitTime; i++) {
                admin = tryConnect(props);
                if (admin == null) {
                    Thread.sleep(1000);
                } else {
                    break;
                }
            }
            var result = admin.describeCluster().clusterId();
            log.info("Cluster ID: {}", result.get(waitTime, TimeUnit.SECONDS));
        } catch (Exception e) {
            log.error("Ka-Boom: {}", e.getMessage());
            System.exit(1);
        } finally {
            if (admin != null) {
                admin.close();
            }
        }

    }

    private Admin tryConnect(Properties props) {
        Admin admin = null;
        try {
            admin = Admin.create(props);
            return admin;
        } catch (Exception e) {
            log.warn("Kafka problem: {}", e.getMessage());
            return null;
        } finally {
            if (admin != null) {
                admin.close();
            }
        }
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

        waitForKafkaToBeUpAndRunning(waitTime);

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
