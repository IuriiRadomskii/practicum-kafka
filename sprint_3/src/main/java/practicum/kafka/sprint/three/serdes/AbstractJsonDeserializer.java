package practicum.kafka.sprint.three.serdes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

@Slf4j
public abstract class AbstractJsonDeserializer<T> implements Deserializer<T> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return mapper.readValue(data, getType());
        } catch (Exception e) {
            log.error("Unable to deserialize type {}: {}", getType(), e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    protected abstract TypeReference<T> getType();

}
