package practicum.kafka.sprint.three.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import practicum.kafka.sprint.three.model.UserMessage;

import java.util.UUID;

import static practicum.kafka.sprint.three.config.StreamsConfiguration.FORBIDDEN_WORDS_STORE;

@Slf4j
public class MaskMessageProcessor implements Processor<UUID, UserMessage, UUID, UserMessage> {

    private KeyValueStore<String, String> forbiddenWordsStore;
    private ProcessorContext<UUID, UserMessage> context;

    @Override
    public void init(ProcessorContext<UUID, UserMessage> context) {
        forbiddenWordsStore = context.getStateStore(FORBIDDEN_WORDS_STORE);
        this.context = context;
    }

    @Override
    public void process(Record<UUID, UserMessage> record) {
        log.info("Processing record: {}", record);
        String masked = record.value().content();
        try (var iterator = forbiddenWordsStore.all()) {
            while (iterator.hasNext()) {
                var forbiddenWord = iterator.next().key;
                log.info("Forbidden word: {}", forbiddenWord);
                masked = masked.replace(forbiddenWord, "***");
            }
        }
        context.forward(new Record<>(record.key(), new UserMessage(record.value().to(), masked), record.timestamp()));
    }
}
