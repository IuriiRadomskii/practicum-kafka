package practicum.kafka.sprint.three.model;

import java.util.UUID;

public record UserMessage(UUID to, String content) {
}
