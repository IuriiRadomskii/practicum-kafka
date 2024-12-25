package practicum.kafka;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class KafkaAdminExample {
    public static void main(String[] args) {
        try (Admin admin = Admin.create(Collections.singletonMap("bootstrap.servers", "localhost:9094"))) { // KRaft broker typically listens on port 9094
            NewTopic newTopic = new NewTopic("my-topic", 1, (short) 1);
            admin.createTopics(Collections.singleton(newTopic)).all().get();
            System.out.println("Topic created successfully in KRaft Mode!");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}