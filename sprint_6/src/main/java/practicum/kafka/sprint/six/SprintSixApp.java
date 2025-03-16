package practicum.kafka.sprint.six;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SprintSixApp {

    public static void main(String[] args) {
        SpringApplication.run(SprintSixApp.class, args);
    }

}
