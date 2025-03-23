package practicum.kafka.sprint.three.serdes;

import org.apache.kafka.common.serialization.Serdes;
import practicum.kafka.sprint.three.model.UserMessage;

public class UserMessageSerde extends Serdes.WrapperSerde<UserMessage> {

    public UserMessageSerde() {
        super(new UserMessageSerializer(), new UserMessageDeserializer());
    }

}
