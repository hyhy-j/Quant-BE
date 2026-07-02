package com.example.quantserver.order.dto;

public record OrderExecuteResponse(
        Long orderId,
        Long userId,
        String stockId,
        String side,
        String status,
        String message
) {
}