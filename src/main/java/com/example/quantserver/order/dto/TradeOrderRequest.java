package com.example.quantserver.order.dto;

import com.example.quantserver.order.enums.OrderSide;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TradeOrderRequest(
        @NotBlank String stockName,
        @NotNull OrderSide side,
        @NotNull @Positive Long quantity,
        @NotNull @Positive BigDecimal orderAmount
) {
}