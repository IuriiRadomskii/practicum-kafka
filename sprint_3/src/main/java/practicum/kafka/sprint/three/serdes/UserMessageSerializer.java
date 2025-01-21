package practicum.kafka.sprint.three.serdes;

import practicum.kafka.sprint.three.model.UserMessage;

public class UserMessageSerializer extends AbstractJsonSerializer<UserMessage> {
    @Override
    public byte[] serialize(String topic, UserMessage data) {
        return getJsonFromPojo(data);
    }
}
