package practicum.kafka.project.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import practicum.kafka.project.dto.User;

@Slf4j
public class JsonDeserializer implements Deserializer<User> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public User deserialize(String topic, byte[] data) {
        try {
            return mapper.readValue(data, User.class);
        } catch (Exception e) {
            log.error("Unable to deserialize transaction status: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
