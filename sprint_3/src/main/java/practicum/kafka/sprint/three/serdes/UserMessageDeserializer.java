package practicum.kafka.sprint.three.serdes;

import com.fasterxml.jackson.core.type.TypeReference;
import practicum.kafka.sprint.three.model.UserMessage;

public class UserMessageDeserializer extends AbstractJsonDeserializer<UserMessage> {

    @Override
    protected TypeReference<UserMessage> getType() {
        return new TypeReference<UserMessage>() {};
    }

}
