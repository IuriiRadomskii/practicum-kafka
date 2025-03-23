package practicum.kafka.sprint.five.dto;

import java.util.UUID;

public record TransactionStatus(
        UUID transactionId,
        String status
) {
}
