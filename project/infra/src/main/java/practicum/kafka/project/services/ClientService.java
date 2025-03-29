package practicum.kafka.project.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import practicum.kafka.project.dto.client.ClientProductSearchRequest;
import practicum.kafka.project.dto.shop.ProductInfo;
import practicum.kafka.project.dto.shop.ProductProjection;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class ClientService {

    private final KafkaProducer<String, ClientProductSearchRequest> producer;
    private final String topic;
    private final ShopService shopService;

    public ClientService(
            KafkaProducer<String, ClientProductSearchRequest> producer,
            @Value("${client.topic}") String topic,
            ShopService shopService
    ) {
        this.producer = producer;
        this.topic = topic;
        this.shopService = shopService;
    }

    public ProductProjection findByName(String name) {
        //producer.send(new ProducerRecord<>(topic, new ClientProductSearchRequest(name)));
        log.info("Client producer sent to topic '{}' request for product '{}'", topic, name);
        return getDummy();
    }

    public List<ProductProjection> getRecommendations() {
        return List.of(getDummy());
    }

    private ProductProjection getDummy() {
        var list = shopService.readProducts();
        var product = list.get(new Random().nextInt(0, list.size()));
        return new ProductProjection(product.name(), product.brand(), product.price().amount().doubleValue());
    }

}
