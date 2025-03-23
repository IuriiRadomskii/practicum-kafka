package practicum.kafka.sprint.five.dto;

public record User(
        String name,
        int favoriteNumber,
        String favoriteColor
) {
}