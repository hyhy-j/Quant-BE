package com.example.quantserver.trade.dto;

public record OrderExecuteResponse(
        Long orderId,
        Long userId,
        String stockId,
        String side,
        String status,
        String message
) {
}