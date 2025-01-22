package practicum.kafka.sprint.three.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import practicum.kafka.sprint.three.model.UserBlockEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static practicum.kafka.sprint.three.config.StreamsConfiguration.USER_BLOCK_EVENTS_STORE;

@Slf4j
public class UserBlockEventsStoreProcessor implements Processor<UUID, UserBlockEvent, UUID, UserBlockEvent> {

    private KeyValueStore<UUID, Set<UUID>> store;

    @Override
    public void init(ProcessorContext<UUID, UserBlockEvent> context) {
        log.info("Blocked user store initialized");
        store = context.getStateStore(USER_BLOCK_EVENTS_STORE);
    }

    @Override
    public void process(Record<UUID, UserBlockEvent> record) {
        var blockedUsers = store.get(record.key());
        if (blockedUsers == null) {
            log.info("Blocked user store does not exist for user {}", record.key());
            blockedUsers = Collections.synchronizedSet(new HashSet<>());
            store.put(record.key(), blockedUsers);
        }
        blockedUsers.add(record.value().blockedUser());
        log.info("User {} blocked user {}", record.key(), record.value().blockedUser());
        store.put(record.key(), blockedUsers);
    }

    @Override
    public void close() {
        Processor.super.close();
    }

}
