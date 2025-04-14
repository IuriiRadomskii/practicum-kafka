package practicum.kafka.project.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

@Slf4j
@Service
public class ProductFilterService {

    private final KafkaProducer<String, String> producer;
    private final StreamsBuilder streamsBuilder;
    private final Properties properties;
    private volatile boolean running = true;

    @Value("${product-filter.enabled}")
    private boolean enabled;
    @Value("${product-filter.topics.filter-names-topic}")
    private String filterTopic;
    @Value("${product-filter.filter-names-list}")
    private List<String> productNames;

    public ProductFilterService(
            StreamsBuilder streamsBuilder,
            @Qualifier("filterStreamProperties") Properties properties,
            @Qualifier("namesProducer") KafkaProducer<String, String> namesProducer
    ) {
        this.streamsBuilder = streamsBuilder;
        this.properties = properties;
        this.producer = namesProducer;
    }

    public void addNames() {
        for (String productName : productNames) {
            producer.send(new ProducerRecord<>(filterTopic, productName, productName));
        }
    }

    public void process() {
        if (!enabled) {
            log.info("Skipping stream processing");
            return;
        }
        try (final KafkaStreams stream = new KafkaStreams(streamsBuilder.build(), properties)) {
            Runtime.getRuntime().addShutdownHook(new Thread(stream::close));
            stream.start();
            while(running) {}
        }
    }

    public void stop() {
        running = false;
    }

}
