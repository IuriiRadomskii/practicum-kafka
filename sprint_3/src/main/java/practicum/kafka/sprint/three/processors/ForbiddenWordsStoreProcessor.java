package practicum.kafka.sprint.three.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import static practicum.kafka.sprint.three.config.StreamsConfiguration.FORBIDDEN_WORDS_STORE;

@Slf4j
public class ForbiddenWordsStoreProcessor implements Processor<String, String, String, String> {
    private KeyValueStore<String, String> store;

    @Override
    public void init(ProcessorContext<String, String> context) {
        log.info("Forbidden words store initialized");
        store = context.getStateStore(FORBIDDEN_WORDS_STORE);
    }

    @Override
    public void process(Record<String, String> record) {
        log.info("Added forbidden word: {}", record);
        store.put(record.key(), record.value());
    }

    @Override
    public void close() {
        Processor.super.close();
    }
}
