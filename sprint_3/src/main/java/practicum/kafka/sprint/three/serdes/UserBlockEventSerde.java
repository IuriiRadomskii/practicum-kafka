package practicum.kafka.sprint.three.serdes;

import org.apache.kafka.common.serialization.Serdes;
import practicum.kafka.sprint.three.model.UserBlockEvent;

public class UserBlockEventSerde extends Serdes.WrapperSerde<UserBlockEvent> {
    public UserBlockEventSerde() {
        super(new UserBlockEventSerializer(), new UserBlockEventDeserializer());
    }
}
