package practicum.kafka.sprint.three.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import practicum.kafka.sprint.three.model.UserMessage;
import practicum.kafka.sprint.three.processors.ForbiddenWordsStoreProcessor;
import practicum.kafka.sprint.three.processors.MaskMessageProcessor;
import practicum.kafka.sprint.three.processors.UserBlockEventsStoreProcessor;
import practicum.kafka.sprint.three.processors.UserBlockProcessor;
import practicum.kafka.sprint.three.serdes.CustomSerdes;
import practicum.kafka.sprint.three.service.MessageService;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StreamsConfiguration {

    public static final String FORBIDDEN_WORDS_STORE = "forbidden_words_store";
    public static final String FORBIDDEN_WORDS_TOPIC = "forbidden_words";
    public static final String USER_MESSAGES_TOPIC = "user_messages";
    public static final String USER_BLOCK_EVENTS_TOPIC = "user_block_events";
    public static final String USER_BLOCK_EVENTS_STORE = "user_block_events_store";

    private final KafkaProperties kafkaProperties;

    private static Map<String, Object> propsAsMap(Properties props) {
        return props.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
    }

    @Bean
    public Properties appProperties() {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "user_message_app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        config.put(StreamsConfig.STATE_DIR_CONFIG, "C:\\Users\\urara\\IdeaProjects\\practicum-kafka\\sprint_3\\tmp");
        return config;
    }

    @Bean
    public StreamsBuilderFactoryBean streamsBuilderFactoryBean(@Qualifier("appProperties") Properties props) {
        return new StreamsBuilderFactoryBean(new KafkaStreamsConfiguration(propsAsMap(props)));
    }

    @Bean("big-bean")
    public KStream<UUID, UserMessage> userMessageKStream(
            StreamsBuilder streamsBuilder,
            MessageService messageService
    ) {

        //user blocks store
        StoreBuilder<KeyValueStore<UUID, Set<UUID>>> blockedUsersStore =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(USER_BLOCK_EVENTS_STORE),
                        Serdes.UUID(),
                        CustomSerdes.setSerde()
                );
        streamsBuilder
                .addStateStore(blockedUsersStore);

        streamsBuilder
                .stream(USER_BLOCK_EVENTS_TOPIC, Consumed.with(Serdes.UUID(), CustomSerdes.userBlockEventSerde()))
                .process(UserBlockEventsStoreProcessor::new, USER_BLOCK_EVENTS_STORE);

        //forbidden words store
        StoreBuilder<KeyValueStore<String, String>> forbiddenWordsStoreBuilder =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(FORBIDDEN_WORDS_STORE),
                        Serdes.String(),
                        Serdes.String()
                );

        streamsBuilder
                .addStateStore(forbiddenWordsStoreBuilder);

        streamsBuilder
                .stream(FORBIDDEN_WORDS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()))
                .process(ForbiddenWordsStoreProcessor::new, FORBIDDEN_WORDS_STORE);


        //main stream
        KStream<UUID, UserMessage> userMessagesStream = streamsBuilder
                .stream(USER_MESSAGES_TOPIC, Consumed.with(Serdes.UUID(), CustomSerdes.userMessageSerde()));

        userMessagesStream
                .process(UserBlockProcessor::new, USER_BLOCK_EVENTS_STORE)
                .process(MaskMessageProcessor::new, FORBIDDEN_WORDS_STORE)
                .foreach(messageService::sendMessage);

        return userMessagesStream;
    }

}
