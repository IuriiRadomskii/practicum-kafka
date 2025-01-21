package practicum.kafka.sprint.three.serdes;

import practicum.kafka.sprint.three.model.UserBlockEvent;

public class UserBlockEventSerializer extends AbstractJsonSerializer<UserBlockEvent> {
    @Override
    public byte[] serialize(String topic, UserBlockEvent data) {
        return getJsonFromPojo(data);
    }
}
