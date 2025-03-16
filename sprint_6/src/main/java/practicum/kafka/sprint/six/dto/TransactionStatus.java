package practicum.kafka.sprint.six.dto;

import java.util.UUID;

public record TransactionStatus(
        UUID transactionId,
        String status
) {
}
