package com.example.quantserver.order.dto;

import java.math.BigDecimal;

public record OrderExecuteResponse(
        String status,
        String stockId,
        String side,
        Long quantity,
        BigDecimal price,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String message
) {
}