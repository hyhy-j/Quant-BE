package com.example.quantserver.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record AgentResponse(
        Map<String, Object> portfolio,
        @JsonProperty("portfolio_reason") String portfolioReason,
        List<Map<String, Object>> orders
) {
}