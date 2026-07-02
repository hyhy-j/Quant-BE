package com.example.quantserver.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record PythonPortfolioResponse(
        Map<String, StockDetail> portfolio,
        @JsonProperty("backtest_result") BacktestResult backtestResult,
        String report,
        @JsonProperty("risk_type") String riskType
) {

    public record StockDetail(
            String name,
            double weight,
            long amount,
            int quantity,
            String reason
    ) {
    }

    public record BacktestResult(
            @JsonProperty("top_stocks") List<String> topStocks,
            List<Double> curve,
            @JsonProperty("monthly_returns") List<Double> monthlyReturns,
            @JsonProperty("expected_return") double expectedReturn,
            double mdd,
            double sharpe
    ) {
    }
}