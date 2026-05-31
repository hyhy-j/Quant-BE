package com.example.quantserver.investment.dto;

import com.example.quantserver.investment.entity.InvestmentProfile;
import com.example.quantserver.investment.enums.ProfileType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvestmentProfileResponse(
        Long id,
        String investmentGoal,
        int riskTolerance,
        String investmentPeriod,
        BigDecimal investableAmount,
        ProfileType profileType,
        LocalDateTime createdAt
) {
    public static InvestmentProfileResponse from(InvestmentProfile profile) {
        return new InvestmentProfileResponse(
                profile.getId(),
                profile.getInvestmentGoal(),
                profile.getRiskTolerance(),
                profile.getInvestmentPeriod(),
                profile.getInvestableAmount(),
                profile.getProfileType(),
                profile.getCreatedAt()
        );
    }
}