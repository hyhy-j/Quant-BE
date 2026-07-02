package com.example.quantserver.trade.dto;

public record OrderStatusCallbackRequest(
        Long orderId,
        Long userId,
        String stockId,
        String side,
        String status,
        String message
) {
}