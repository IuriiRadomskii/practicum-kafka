package practicum.kafka.sprint.three.serdes;

import com.fasterxml.jackson.core.type.TypeReference;
import practicum.kafka.sprint.three.model.UserBlockEvent;

public class UserBlockEventDeserializer extends AbstractJsonDeserializer<UserBlockEvent> {

    @Override
    protected TypeReference<UserBlockEvent> getType() {
        return new TypeReference<UserBlockEvent>() {};
    }

}
