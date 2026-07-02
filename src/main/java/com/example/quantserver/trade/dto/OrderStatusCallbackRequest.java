package com.example.quantserver.trade.dto;

public record OrderStatusCallbackRequest(
        String orderId,
        Long userId,
        String stockId,
        String side,
        String status,
        String message
) {
}