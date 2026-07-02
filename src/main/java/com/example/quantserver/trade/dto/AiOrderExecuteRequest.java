package com.example.quantserver.trade.dto;

import com.example.quantserver.trade.enums.OrderSide;

public record AiOrderExecuteRequest(
        Long userId,
        String stockId,
        OrderSide side,
        Long amount
) {
}