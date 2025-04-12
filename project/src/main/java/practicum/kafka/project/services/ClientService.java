package practicum.kafka.project.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import practicum.kafka.project.dto.client.ClientRequest;
import practicum.kafka.project.dto.shop.ProductProjection;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class ClientService {

    private final KafkaProducer<UUID, ClientRequest> producer;
    private final KafkaConsumer<UUID, ProductProjection> consumer;
    private final KafkaConsumer<UUID, ProductProjection> consumerRecommendation;
    private final String producerTopic;
    private final String consumerTopic;
    private final String consumerRecommendationTopic;
    private final int pollDuration;
    private final int pollCount;

    public Optional<ProductProjection> findByName(String name) {
        var requestKey = UUID.randomUUID();
        producer.send(new ProducerRecord<>(producerTopic, requestKey, new ClientRequest(name)));
        log.info("Client producer sent to topic '{}' request for product '{}'", producerTopic, name);
        consumer.subscribe(List.of(consumerTopic));
        for (int i = 0; i < pollCount; i++) {
            ConsumerRecords<UUID, ProductProjection> records = consumer.poll(Duration.ofSeconds(pollDuration));
            for (ConsumerRecord<UUID, ProductProjection> record : records) {
                if (record.key().equals(requestKey)) {
                    var result = record.value();
                    consumer.commitSync();
                    consumer.unsubscribe();
                    return Optional.of(result);
                }
            }
        }
        log.warn("Product {} not found", name);
        return Optional.empty();
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

}
