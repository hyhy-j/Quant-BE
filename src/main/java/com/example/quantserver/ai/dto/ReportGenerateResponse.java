package com.example.quantserver.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record ReportGenerateResponse(
        @JsonProperty("sentiment_scores") Map<String, SentimentScore> sentimentScores,
        @JsonProperty("portfolio_reason") String portfolioReason
) {
    public record SentimentScore(
            double score,
            int count
    ) {
    }
}