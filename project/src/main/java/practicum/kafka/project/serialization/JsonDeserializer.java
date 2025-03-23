package practicum.kafka.project.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

@Slf4j
public class JsonDeserializer implements Deserializer<Object> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Object deserialize(String topic, byte[] data) {
        try {
            return mapper.readValue(data, Object.class);
        } catch (Exception e) {
            log.error("Unable to deserialize transaction status: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
