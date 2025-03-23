package practicum.kafka.sprint.three.examples;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SimpleKafkaStreamsExample {

    static final ExecutorService executor = Executors.newFixedThreadPool(5);
    static final String LEADER_HOST = "localhost:9094,localhost:9095,localhost:9096";

    public static void main(String[] args) {
        try {
            // Конфигурация Kafka Streams
            Properties config = new Properties();
            config.put(StreamsConfig.APPLICATION_ID_CONFIG, "simple-kafka-streams-app");
            config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, LEADER_HOST);
            config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
            config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

            // Создание топологии
            StreamsBuilder builder = new StreamsBuilder();
            KStream<String, String> inputStream = builder.stream("input_topic");

            // Обработка данных
            inputStream.mapValues(value -> {
                log.info("Processing in stream {}", value);
                return "Processed: " + value;
            }).to("output_topic");

            // Инициализация Kafka Streams
            KafkaStreams streams = new KafkaStreams(builder.build(), config);

            // Запуск приложения
            streams.start();

            log.info("Kafka Streams приложение запущено успешно.");

            executor.submit(() -> {
                        try (var producer = producer(LEADER_HOST)) {
                            for (var i = 0; i < 1000; i++) {
                                var future = producer.send(new ProducerRecord<>("input_topic", "Task: " + 1));
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
                            }
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
            );

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    static KafkaProducer<String, String> producer(String leaderHost) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, leaderHost);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2");
        props.put(ProducerConfig.RETRIES_CONFIG, 10);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 500);
        props.put(ProducerConfig.RETRY_BACKOFF_MAX_MS_CONFIG, 5000);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }


    static KafkaConsumer<String, String> consumer(String leaderHost) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, leaderHost);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return new KafkaConsumer<>(props);
    }

}