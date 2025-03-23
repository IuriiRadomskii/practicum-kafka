package practicum.kafka.project.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;
import practicum.kafka.project.dto.client.ClientRequest;

@Slf4j
public class ClientRequestSerializer implements Serializer<ClientRequest> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, ClientRequest data) {
        try {
            return mapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize data: {}", data.toString(), e);
            throw new RuntimeException(e);
        }
    }
}
