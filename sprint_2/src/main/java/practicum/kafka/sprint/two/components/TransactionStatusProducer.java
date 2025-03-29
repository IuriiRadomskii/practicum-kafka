package practicum.kafka.sprint.two.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import practicum.kafka.sprint.two.dto.TransactionStatus;

import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionStatusProducer {

    private final KafkaProducer<String, TransactionStatus> producer;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public void send(String topic, TransactionStatus message) {
        ProducerRecord<String, TransactionStatus> record = new ProducerRecord<>(topic, message);
        executorService.submit(() -> {
            var future = producer.send(record);
            try {
                var metadata = future.get(5, TimeUnit.SECONDS);
                log.info("Sent to topic: {}, partition: {}, offset; {}",
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset()
                );
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

}
