package practicum.kafka.project.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import practicum.kafka.project.dto.client.ClientProductSearchRequest;
import practicum.kafka.project.dto.shop.ProductInfo;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class ClientService {

    private final KafkaProducer<String, ClientProductSearchRequest> producer;
    private final String topic;

    public ClientService(
            KafkaProducer<String, ClientProductSearchRequest> producer,
            @Value("${client.topic}") String topic
    ) {
        this.producer = producer;
        this.topic = topic;
    }

    public ProductInfo findByName(String name) {
        producer.send(new ProducerRecord<>(topic, new ClientProductSearchRequest(name)));
        log.info("Client producer sent to topic '{}' request for product '{}'", topic, name);
        return null;
    }

    public List<ProductInfo> getRecommendations() {
        return Collections.emptyList();
    }

}
