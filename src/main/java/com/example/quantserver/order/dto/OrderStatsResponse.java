package com.example.quantserver.order.dto;

public record OrderStatsResponse(
        PnlInfo daily,
        PnlInfo weekly,
        PnlInfo monthly
) {
}