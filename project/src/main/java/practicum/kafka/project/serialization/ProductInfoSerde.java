package practicum.kafka.project.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import practicum.kafka.project.dto.shop.ProductInfo;

@Slf4j
public class ProductInfoSerde extends Serdes.WrapperSerde<ProductInfo> {

    public ProductInfoSerde() {
        super(getSerializer(), getDeserializer());
    }

    public static Serializer<ProductInfo> getSerializer() {
        return new Serializer<ProductInfo>() {
            private ObjectMapper mapper = new ObjectMapper();
            @Override
            public byte[] serialize(String topic, ProductInfo data) {
                try {
                    return mapper.writeValueAsBytes(data);
                } catch (JsonProcessingException e) {
                    log.error("Unable to serialize data: {}", data.toString(), e);
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public static Deserializer<ProductInfo> getDeserializer() {
        return new Deserializer<ProductInfo>() {
            private ObjectMapper mapper = new ObjectMapper();

            @Override
            public ProductInfo deserialize(String topic, byte[] data) {
                try {
                    return mapper.readValue(data, ProductInfo.class);
                } catch (Exception e) {
                    log.error("Unable to deserialize transaction status: {}", e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        };
    }

}
