package practicum.kafka.project.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import practicum.kafka.project.dto.shop.ProductInfo;

import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class ProductFilterProcessor implements Processor<UUID, ProductInfo, UUID, ProductInfo> {

    private final String storeName;
    private KeyValueStore<String, Set<String>> store;
    private ProcessorContext<UUID, ProductInfo> context;

    @Override
    public void init(ProcessorContext<UUID, ProductInfo> context) {
        log.info("Filter processor init");
        store = context.getStateStore(storeName);
        this.context = context;
    }

    @Override
    public void process(Record<UUID, ProductInfo> record) {
        if (store == null) {
            log.error("Filter processor has not been initialized");
            return;
        }
        var productInfo = record.value();
        var names = store.get("key");
        for (String name : names) {
            if (productInfo.getName().startsWith(name)) {
                log.info("Skipped product {}:{}", productInfo.getProduct_id(), productInfo.getName());
                return;
            }
        }
        context.forward(record);
    }

    @Override
    public void close() {
        Processor.super.close();
    }
}
