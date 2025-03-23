package practicum.kafka.project.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class FilterNamesStoreProcessor implements Processor<String, String, String, String> {

    private final String storeName;
    private KeyValueStore<String, Set<String>> store;

    @Override
    public void init(ProcessorContext<String, String> context) {
        log.info("Filter names store processor init");
        store = context.getStateStore(storeName);
        store.put("key", new HashSet<String>());
    }

    @Override
    public void process(Record<String, String> record) {
        log.info("Added filter product name: {}", record);
        var names = store.get("key");
        names.add(record.key());
        store.put("key", names);
    }

    @Override
    public void close() {
        Processor.super.close();
    }

}