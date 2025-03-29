package practicum.kafka.project.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import practicum.kafka.project.dto.shop.ProductInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShopService {

    private final KafkaProducer<String, ProductInfo> producer;
    private final String topic;
    private final long delay;

    public ShopService(
            KafkaProducer<String, ProductInfo> producer,
            @Value("${shop.topic}") String topic,
            @Value("${shop.delay}") long delay
    ) {
        this.producer = producer;
        this.topic = topic;
        this.delay = delay;
    }

    public void sendProduct(ProductInfo product) {
        producer.send(new ProducerRecord<>(topic, product.product_id(), product));
        producer.flush();
    }

    public void readAndSendProducts() {
        List<ProductInfo> products = readProducts();
        products.forEach(product -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                log.warn("Shop delay interrupted: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
            producer.send(new ProducerRecord<>(topic, product.product_id(), product));
            log.info("Sent product with id: {}", product.product_id());
        });
        producer.flush();
    }

    public List<ProductInfo> readProducts() {
        Path dir = Path.of(System.getProperty("user.dir")).resolve(Paths.get("project", "src", "main", "resources", "products"));
        log.info("Reading products from {}", dir);
        var mapper = new ObjectMapper();
        try (var files = Files.list(dir)) {
            return files
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        try {
                            return mapper.readValue(path.toFile(), ProductInfo.class);
                        } catch (IOException e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Error while reading product info file", e);
            return Collections.emptyList();
        }
    }

}
