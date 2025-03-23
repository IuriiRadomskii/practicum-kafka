package practicum.kafka.project.serialization;

import org.apache.kafka.common.serialization.Serdes;

import java.util.Set;
import java.util.UUID;

public class SetSerde extends Serdes.WrapperSerde<Set<String>> {
    public SetSerde() {
        super(new SetSerializer(), new SetDeserializer());
    }
}
