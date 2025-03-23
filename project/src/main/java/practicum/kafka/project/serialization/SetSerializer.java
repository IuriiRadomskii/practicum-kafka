package practicum.kafka.project.serialization;

import java.util.Set;
import java.util.UUID;

public class SetSerializer extends AbstractJsonSerializer<Set<String>> {

    @Override
    public byte[] serialize(String topic, Set<String> data) {
        return getJsonFromPojo(data);
    }
}
