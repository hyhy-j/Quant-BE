package com.example.quantserver.order.dto;

import com.example.quantserver.order.enums.OrderSide;

public record AiOrderExecuteRequest(
        Long userId,
        String stockId,
        OrderSide side,
        Long amount
) {
}