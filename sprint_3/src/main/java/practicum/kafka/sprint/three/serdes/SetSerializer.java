package practicum.kafka.sprint.three.serdes;

import java.util.Set;
import java.util.UUID;

public class SetSerializer extends AbstractJsonSerializer<Set<UUID>> {

    @Override
    public byte[] serialize(String topic, Set<UUID> data) {
        return getJsonFromPojo(data);
    }
}
