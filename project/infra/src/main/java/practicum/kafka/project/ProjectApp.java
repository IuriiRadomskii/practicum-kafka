package practicum.kafka.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ProjectApp {

    public static void main(String[] args) {
        SpringApplication.run(ProjectApp.class, args);
    }

}
