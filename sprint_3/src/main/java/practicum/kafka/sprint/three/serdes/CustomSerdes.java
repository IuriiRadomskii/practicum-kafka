package practicum.kafka.sprint.three.serdes;

public class CustomSerdes {

    public static UserMessageSerde userMessageSerde() {
        return new UserMessageSerde();
    }

    public static UserBlockEventSerde userBlockEventSerde() {
        return new UserBlockEventSerde();
    }

    public static SetSerde setSerde() {
        return new SetSerde();
    }

}
