package practicum.kafka.sprint.three.serdes;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Set;
import java.util.UUID;

public class SetDeserializer extends AbstractJsonDeserializer<Set<UUID>>{
    @Override
    protected TypeReference<Set<UUID>> getType() {
        return new TypeReference<Set<UUID>>() {
        };
    }
}
