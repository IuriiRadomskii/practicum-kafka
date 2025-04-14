package practicum.kafka.project.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import practicum.kafka.project.dto.shop.ProductInfo;
import practicum.kafka.project.dto.shop.ProductProjection;

@Slf4j
public class ProductProjectionDeserializer implements Deserializer<ProductProjection> {
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public ProductProjection deserialize(String topic, byte[] data) {
        try {
            return mapper.readValue(data, ProductProjection.class);
        } catch (Exception e) {
            log.error("Unable to deserialize transaction status: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
