package practicum.kafka.sprint.three.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import practicum.kafka.sprint.three.model.UserBlockEvent;

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
        var set = store.get(record.key());
        if (set == null) {
            store.put(record.key(), new HashSet<>());
            return;
        }
        set.add(record.value().blockedUser());
        store.put(record.key(), set);
    }

    @Override
    public void close() {
        Processor.super.close();
    }

}
