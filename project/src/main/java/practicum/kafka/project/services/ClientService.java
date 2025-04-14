package practicum.kafka.project.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import practicum.kafka.project.dto.client.ClientRequest;
import practicum.kafka.project.dto.shop.ProductInfo;
import practicum.kafka.project.dto.shop.ProductProjection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class ClientService {

    @Value("${client.consumer-test-topic}")
    private String testTopic;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final KafkaProducer<UUID, ClientRequest> producer;
    private final KafkaConsumer<UUID, ProductProjection> consumer;
    private final KafkaConsumer<UUID, ProductProjection> consumerRecommendation;
    private final KafkaConsumer<UUID, ProductInfo> testConsumer;
    private final String producerTopic;
    private final String consumerTopic;
    private final String consumerRecommendationTopic;
    private final int pollDuration;
    private final int pollCount;

    public List<ProductProjection> findByName(String name) {
        var requestKey = UUID.randomUUID();
        producer.send(new ProducerRecord<>(producerTopic, requestKey, new ClientRequest(name)));
        log.info("Client producer sent to topic '{}' request for product '{}'", producerTopic, name);
        return getProductFromDB(name)
                .stream()
                .map(p -> new ProductProjection(p.getName(), p.getDescription(), p.getStock().available()))
                .toList();
    }

    public List<ProductProjection> getRecommendations() {
        consumerRecommendation.subscribe(List.of(consumerRecommendationTopic));
        for (int i = 0; i < pollCount; i++) {
            ConsumerRecords<UUID, ProductProjection> records = consumerRecommendation.poll(Duration.ofSeconds(pollDuration));
            if (records.count() == 0) {
                log.info("Recommendation of {} not found", consumerRecommendationTopic);
                continue;
            }
            List<ProductProjection> result = new ArrayList<>();
            records.forEach(record -> result.add(record.value()));
            consumerRecommendation.commitSync();
            consumerRecommendation.unsubscribe();
            return result;
        }
        log.warn("Recommendation of {} not found", consumerRecommendationTopic);
        return List.of();
    }

    public List<ProductInfo> getTestProducts() {
        testConsumer.subscribe(List.of(testTopic));
        for (int i = 0; i < pollCount; i++) {
            ConsumerRecords<UUID, ProductInfo> records = testConsumer.poll(Duration.ofSeconds(pollDuration));
            if (records.count() == 0) {
                log.info("Recommendation of {} not found", testConsumer);
                continue;
            }
            List<ProductInfo> result = new ArrayList<>();
            records.forEach(record -> result.add(record.value()));
            testConsumer.commitSync();
            testConsumer.unsubscribe();
            return result;
        }
        log.warn("Recommendation of {} not found", testConsumer);
        return List.of();
    }

    private List<ProductInfo> getProductFromDB(String name) {
        Path dataProductsTopicOutFile = Path.of(System.getProperty("user.dir"),
                "project", "infra", "output", "data-products-topic.out");
        if (!Files.exists(dataProductsTopicOutFile) || !Files.isRegularFile(dataProductsTopicOutFile)) {
            log.warn("Could not find product information file '{}'", dataProductsTopicOutFile);
            return List.of();
        }
        List<ProductInfo> result = new ArrayList<>();
        try {
            var lines = Files.readAllLines(dataProductsTopicOutFile);
            for (String line : lines) {
                try {
                    var productInfo = objectMapper.readValue(line, ProductInfo.class);
                    if (productInfo.getName().toLowerCase().startsWith(name.toLowerCase())) {
                        result.add(productInfo);
                    }
                } catch (Exception e) {
                    log.warn("Could not parse line '{}'", line);
                }
            }
        } catch (IOException e) {
            log.error("Error reading product information file '{}'", dataProductsTopicOutFile, e);
            return List.of();
        }
        log.warn("Product {} not found", name);
        return result;
    }

}
