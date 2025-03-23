package practicum.kafka.sprint.two;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OneProducerTwoConsumersApp {

    public static void main(String[] args) {
        SpringApplication.run(OneProducerTwoConsumersApp.class, args);
    }

}
