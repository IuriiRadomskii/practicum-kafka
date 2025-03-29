package practicum.kafka.sprint.four.connector;

public record MetricEvent(String name, String type, String description, String value) {}
