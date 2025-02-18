package practicum.kafka.sprint.five.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import practicum.kafka.sprint.five.dto.TransactionStatus;

@Slf4j
public class JsonSerializer implements Serializer<TransactionStatus> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, TransactionStatus data) {
        try {
            return mapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new SerializationException("Error serializing TransactionStatus data", e);
        }
    }

}
