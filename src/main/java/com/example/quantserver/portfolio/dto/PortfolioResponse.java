package com.example.quantserver.portfolio.dto;

import com.example.quantserver.portfolio.entity.PortfolioItem;
import com.example.quantserver.portfolio.entity.PortfolioRecommendation;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record PortfolioResponse(
        Map<String, StockInfo> portfolio,
        @JsonProperty("backtest_result") BacktestResult backtestResult,
        String report,
        @JsonProperty("risk_type") String riskType,
        @JsonProperty("created_at") LocalDateTime createdAt
) {

    public record StockInfo(
            String name,
            BigDecimal weight,
            BigDecimal amount,
            Long quantity,
            String reason
    ) {
    }

    public record BacktestResult(
            @JsonProperty("top_stocks") List<String> topStocks,
            List<Double> curve,
            @JsonProperty("monthly_returns") List<Double> monthlyReturns,
            @JsonProperty("expected_return") BigDecimal expectedReturn,
            BigDecimal mdd,
            BigDecimal sharpe
    ) {
    }

    public static PortfolioResponse of(PortfolioRecommendation rec, List<PortfolioItem> items,
                                       List<String> topStocks, List<Double> curve,
                                       List<Double> monthlyReturns) {
        Map<String, StockInfo> portfolio = new LinkedHashMap<>();
        items.forEach(item -> portfolio.put(
                item.getStockCode(),
                new StockInfo(item.getName(), item.getWeight(), item.getAmount(), item.getQuantity(), item.getReason())
        ));

        BacktestResult backtestResult = new BacktestResult(
                topStocks, curve, monthlyReturns,
                rec.getExpectedReturn(), rec.getMdd(), rec.getSharpeRatio()
        );

        return new PortfolioResponse(
                portfolio,
                backtestResult,
                rec.getReport(),
                rec.getRiskType() != null ? rec.getRiskType().name() : null,
                rec.getCreatedAt()
        );
    }
}