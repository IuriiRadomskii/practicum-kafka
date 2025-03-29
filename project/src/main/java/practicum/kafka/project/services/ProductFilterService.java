package practicum.kafka.project.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Service
public class ProductFilterService {

    private final StreamsBuilder streamsBuilder;
    private final Properties properties;
    private List<UUID> forbiddenList;
    private KafkaProducer<UUID, UUID> forbiddenProductsProducer;
    private String forbiddenProductsTopic;

    public ProductFilterService(
            StreamsBuilder streamsBuilder,
            @Qualifier("filterStreamProperties") Properties properties,
            @Value("${product-filter.forbidden-product-ids}") List<UUID> forbiddenList,
            @Value("${product-filter.topics.forbidden-list}") String topic,
            @Qualifier("forbiddenProductsProducer") KafkaProducer<UUID, UUID> forbiddenProductsProducer
    ) {
        this.streamsBuilder = streamsBuilder;
        this.properties = properties;
        this.forbiddenList = forbiddenList;
        this.forbiddenProductsProducer = forbiddenProductsProducer;
        this.forbiddenProductsTopic = topic;
    }

    public void addForbiddenProductsToTopic() {

    }

    public void process() {
        try (final KafkaStreams stream = new KafkaStreams(streamsBuilder.build(), properties)) {
            Runtime.getRuntime().addShutdownHook(new Thread(stream::close));
            stream.start();
        }
    }

}
