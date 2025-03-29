package practicum.kafka.sprint.three.serdes;

import org.apache.kafka.common.serialization.Serdes;

import java.util.Set;
import java.util.UUID;

public class SetSerde extends Serdes.WrapperSerde<Set<UUID>> {
    public SetSerde() {
        super(new SetSerializer(), new SetDeserializer());
    }
}
