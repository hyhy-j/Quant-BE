package com.example.quantserver.order.dto;

import com.example.quantserver.order.entity.TradeOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeOrderResponse(
        Long id,
        String stockCode,
        String side,
        Long quantity,
        BigDecimal price,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String status,
        LocalDateTime executedAt
) {
    public static TradeOrderResponse from(TradeOrder order) {
        return new TradeOrderResponse(
                order.getId(),
                order.getStockCode(),
                order.getSide().name(),
                order.getQuantity(),
                order.getPrice(),
                order.getAmount(),
                order.getBalanceAfter(),
                order.getStatus(),
                order.getExecutedAt()
        );
    }
}