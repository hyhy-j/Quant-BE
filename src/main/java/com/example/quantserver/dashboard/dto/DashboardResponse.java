package com.example.quantserver.dashboard.dto;

import com.example.quantserver.order.dto.PnlInfo;
import com.example.quantserver.report.dto.MarketReportResponse;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        AssetSummary asset,
        List<StockSummary> stocks,
        MarketReportResponse todayReport
) {

    public record AssetSummary(
            BigDecimal totalAssets,
            PnlInfo todayPnl,
            PnlInfo cumulativePnl
    ) {
    }

    public record StockSummary(
            String stockCode,
            String stockName,
            BigDecimal currentPrice,
            Long quantity,
            BigDecimal avgPrice,
            BigDecimal totalValue,
            PnlInfo unrealizedPnl
    ) {
    }
}