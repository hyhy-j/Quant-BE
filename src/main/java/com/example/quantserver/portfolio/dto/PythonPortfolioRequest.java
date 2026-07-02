package com.example.quantserver.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record PythonPortfolioRequest(
        @JsonProperty("user_id") Long userId,
        String investmentGoal,
        int riskTolerance,
        String investmentPeriod,
        BigDecimal investableAmount,
        String profileType
) {
}