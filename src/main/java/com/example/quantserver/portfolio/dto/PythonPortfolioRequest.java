package com.example.quantserver.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record PythonPortfolioRequest(
        @JsonProperty("user_id") Long userId,
        @JsonProperty("investment_goal") String investmentGoal,
        @JsonProperty("risk_tolerance") int riskTolerance,
        @JsonProperty("investment_period") String investmentPeriod,
        @JsonProperty("investable_amount") BigDecimal investableAmount,
        @JsonProperty("profile_type") String profileType
) {
}