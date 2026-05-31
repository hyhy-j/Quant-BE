package com.example.quantserver.investment.dto;

import com.example.quantserver.investment.enums.InvestmentPeriod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record InvestmentProfileRequest(
        @NotBlank String investmentGoal,
        @Min(1) @Max(5) int riskTolerance,
        @NotNull InvestmentPeriod investmentPeriod,
        @NotNull @Positive BigDecimal investableAmount
) {
}