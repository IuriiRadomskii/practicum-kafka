package practicum.kafka.project.serialization;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import practicum.kafka.project.dto.client.ClientRequest;

@Slf4j
public class ClientRequestSerde extends Serdes.WrapperSerde<ClientRequest> {

    public ClientRequestSerde() {
        super(new ClientRequestSerializer(), new ClientRequestDeserializer());
    }

}
