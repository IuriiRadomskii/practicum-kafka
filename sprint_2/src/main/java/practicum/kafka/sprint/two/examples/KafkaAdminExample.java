package practicum.kafka.sprint.two.examples;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Slf4j
public class KafkaAdminExample {
    public static void main(String[] args) {
        declareTopic(List.of(
                new NewTopic("topic_4", 3, (short) 3),
                new NewTopic("topic_5", 1, (short) 4)
        ));
    }

    static void declareTopic(List<NewTopic> topics) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094");

        try (Admin admin = Admin.create(props)) {
            var result = admin.createTopics(topics).all();
            long startTime = System.currentTimeMillis();
            while (true) {
                if (!result.isDone()) {
                    continue;
                } else {
                    result.get();
                    break;
                }
            }
            long endTime = System.currentTimeMillis();
            log.info("Topic created in {} ms", endTime - startTime);
        } catch (ExecutionException e) {
            log.error("Execution error during topic creation: {}", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Topic creation was interrupted: {}", e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred: {}", e.getMessage());
        }
    }

}