package practicum.kafka.project.dto.shop;

import java.math.BigDecimal;

public record Price (
    BigDecimal amount,
    String currency
) {}