package com.example.quantserver.order.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderStatsResponse(
        PnlInfo daily,
        PnlInfo weekly,
        PnlInfo monthly,
        List<HistoryPoint> history
) {

    public record HistoryPoint(
            LocalDate date,
            BigDecimal totalAssets
    ) {
    }
}