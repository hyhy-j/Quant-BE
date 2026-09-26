package com.example.quantserver.order.dto;

import java.math.BigDecimal;

public record TradableStockResponse(
        String code,
        String name,
        BigDecimal price
) {
}
