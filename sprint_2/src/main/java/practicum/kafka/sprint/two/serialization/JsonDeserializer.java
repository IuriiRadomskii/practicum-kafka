package practicum.kafka.sprint.two.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import practicum.kafka.sprint.two.dto.TransactionStatus;

@Slf4j
public class JsonDeserializer implements Deserializer<TransactionStatus> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public TransactionStatus deserialize(String topic, byte[] data) {
        try {
            return mapper.readValue(data, TransactionStatus.class);
        } catch (Exception e) {
            log.error("Unable to deserialize transaction status: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
