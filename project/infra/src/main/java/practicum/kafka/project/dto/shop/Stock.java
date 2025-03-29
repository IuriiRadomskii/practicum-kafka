package practicum.kafka.project.dto.shop;

public record Stock (
    int available,
    int reserved
) {}