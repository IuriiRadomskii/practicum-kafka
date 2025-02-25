package practicum.kafka.sprint.three.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import practicum.kafka.sprint.three.model.UserMessage;

import java.util.Set;
import java.util.UUID;

import static practicum.kafka.sprint.three.config.StreamsConfiguration.USER_BLOCK_EVENTS_STORE;

@Slf4j
public class UserBlockProcessor implements Processor<UUID, UserMessage, UUID, UserMessage> {
    private KeyValueStore<UUID, Set<UUID>> blockedUsersStore;
    private ProcessorContext<UUID, UserMessage> context;

    @Override
    public void init(ProcessorContext<UUID, UserMessage> context) {
        log.info("Mask processor init");
        blockedUsersStore = context.getStateStore(USER_BLOCK_EVENTS_STORE);
        this.context = context;
    }

    @Override
    public void process(Record<UUID, UserMessage> record) {
        log.info("Block user processor record: {}", record);
        var sender = record.key();
        log.info("Sender {}", sender.toString());
        var receiver = record.value().to();
        log.info("Receiver: {}", receiver);
        var blockedUsers = blockedUsersStore.get(receiver);
        log.info("Blocked users: {}", blockedUsers);
        if (blockedUsers == null || blockedUsers.isEmpty() || !blockedUsers.contains(sender)) {
            context.forward(record);
        }
        log.info("Receiver {} blocked Sender {}", receiver, sender);
    }

    @Override
    public void close() {
        Processor.super.close();
    }
}
