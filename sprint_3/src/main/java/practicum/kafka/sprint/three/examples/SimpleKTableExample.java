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
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.Stores;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SimpleKTableExample {

    static final ExecutorService executor = Executors.newFixedThreadPool(5);
    static final String LEADER_HOST = "localhost:9094,localhost:9095,localhost:9096";
    static final int NUMBER_OF_MESSAGES = 50;

    public static void main(String[] args) {
        // Создание StreamsBuilder
        StreamsBuilder builder = new StreamsBuilder();

        // Создание потока из Kafka-топика
        KStream<String, String> stream = builder.stream("input_topic",
                Consumed.with(Serdes.String(), Serdes.String()));

        // Преобразование потока в таблицу с помощью метода toTable()
        KTable<String, String> table = stream.toTable(
                Materialized.<String, String>
                                as(Stores.persistentKeyValueStore("word_count_store"))
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.String())
        );

        table
                .groupBy(((key, value) -> {
                    if (key.length() > UUID.randomUUID().toString().length()) {
                        return new KeyValue<>(UUID.randomUUID(), "MORE_MORE" + value.toString());
                    } else {
                        return new KeyValue<>(UUID.randomUUID(), "LESS" + value.toString());
                    }
                }))
                .count()
                .toStream()
                .map((key, value) -> {
                    log.info("COUNTED: " + key + " : " + value);
                    return new KeyValue<>(key, value + "NEW_VALUE");
                }).to("output_topic");

        startStream(builder, getConfig("ktable-app"));
        startProduce();
        startConsume();
    }

    public static Properties getConfig(String appName) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, appName);
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, LEADER_HOST);
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        return config;
    }

    static void startStream(StreamsBuilder builder, Properties config) {
        executor.submit(() -> {
            try (KafkaStreams streams = new KafkaStreams(builder.build(), config)) {
                streams.start();
                log.info("Kafka Streams приложение запущено успешно.");
                while (true) {
                }
            }
        });
    }

    static void startProduce() {
        executor.submit(() -> {
                    try (var producer = producer(LEADER_HOST)) {
                        for (var i = 0; i < NUMBER_OF_MESSAGES; i++) {
                            var future = producer.send(new ProducerRecord<>(
                                    "input_topic",
                                    UUID.randomUUID().toString(),
                                    UUID.randomUUID().toString() + "-VALUE")
                            );
                            try {
                                var metadata = future.get(5, TimeUnit.SECONDS);
                                log.debug("Sent to topic: {}, partition: {}, offset; {}",
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
    }

    static void startConsume() {
        executor.submit(() -> {
            try (var consumer = consumer(LEADER_HOST)) {
                consumer.subscribe(List.of("output_topic"));
                while (true) {
                    var result = consumer.poll(Duration.ofMillis(1000));
                    result.forEach(x -> log.info("\n\nReceived: {}\n\n", x));
                    consumer.commitSync();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
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
