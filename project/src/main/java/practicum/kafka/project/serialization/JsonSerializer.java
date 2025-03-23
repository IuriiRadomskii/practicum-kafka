package practicum.kafka.project.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import practicum.kafka.project.dto.User;

@Slf4j
public class JsonSerializer implements Serializer<User> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, User data) {
        try {
            return mapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new SerializationException("Error serializing TransactionStatus data", e);
        }
    }

}
