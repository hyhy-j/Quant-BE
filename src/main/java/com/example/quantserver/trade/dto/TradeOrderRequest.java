package com.example.quantserver.trade.dto;

import com.example.quantserver.trade.enums.OrderSide;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TradeOrderRequest(
        @NotBlank String stockName,
        @NotNull OrderSide side,
        @NotNull @Positive Long quantity
) {
}