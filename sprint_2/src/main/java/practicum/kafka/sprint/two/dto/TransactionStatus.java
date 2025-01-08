package practicum.kafka.sprint.two.dto;

import java.util.UUID;

public record TransactionStatus(
        UUID transactionId,
        String status
) {
}
