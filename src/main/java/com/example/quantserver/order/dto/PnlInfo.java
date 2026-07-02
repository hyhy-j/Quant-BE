package com.example.quantserver.order.dto;

import java.math.BigDecimal;

public record PnlInfo(
        BigDecimal amount,
        BigDecimal rate
) {
}