package com.example.quantserver.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AgentRequest(
        @JsonProperty("user_id") Long userId,
        @JsonProperty("risk_level") String riskLevel,
        @JsonProperty("investment_amount") Long investmentAmount
) {
}