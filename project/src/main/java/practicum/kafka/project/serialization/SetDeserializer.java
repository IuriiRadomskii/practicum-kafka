package practicum.kafka.project.serialization;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Set;
import java.util.UUID;

public class SetDeserializer extends AbstractJsonDeserializer<Set<String>>{
    @Override
    protected TypeReference<Set<String>> getType() {
        return new TypeReference<Set<String>>() {
        };
    }
}
