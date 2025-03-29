package practicum.kafka.project.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import practicum.kafka.project.config.ClusterCommonProperties;
import practicum.kafka.project.dto.shop.ProductInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;

//meh
@Slf4j
@Service
@RequiredArgsConstructor
public class SortOfElasticSearchService implements InitializingBean {

    private final ClusterCommonProperties clusterCommonProperties;
    private ClusterCommonProperties properties;
    private final Object lock = new Object();
    private final Path file = Path.of(System.getProperty("user.dir"))
            .resolve("products_storage");
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${hdfs.user.username}")
    private String consumerUsername;
    @Value("${hdfs.user.password}")
    private String consumerPassword;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    public void consumeProducts() {
        var props = clusterCommonProperties.getCommonConsumerProperties(consumerUsername, consumerPassword);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "etl-consumer");
        KafkaConsumer<String, ProductInfo> consumer = new KafkaConsumer<>(props);
        var records = consumer.poll(Duration.ofMillis(1000));
        for (ConsumerRecord<String, ProductInfo> record : records) {
            addProduct(record.value());
        }
        consumer.commitSync();
        consumer.close();
    }

    public void addProduct(ProductInfo productInfo) {
        synchronized (lock) {
            try {
                var productString = objectMapper.writeValueAsString(productInfo);
                Files.write(file, productString.getBytes(), StandardOpenOption.APPEND);
            } catch (Exception e) {
                log.error("Unable to add product: {}", e.getMessage(), e);
            }
        }
    }

    public ProductInfo getProductByName(String productName) {
        synchronized (lock) {
            try {
                var lines = Files.readAllLines(file);
                for (String line : lines) {
                    var productInfo = objectMapper.readValue(line, ProductInfo.class);
                    if (productInfo.name().equals(productName)) {
                        log.info("Found product: {}", productName);
                        return productInfo;
                    }
                }
                log.info("Product with name {} not found", productName);
                return null;
            } catch (Exception e) {
                log.error("Error getting product by name: {}", e.getMessage(), e);
                return null;
            }
        }
    }

}
