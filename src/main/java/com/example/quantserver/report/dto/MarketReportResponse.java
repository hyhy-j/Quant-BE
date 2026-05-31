package com.example.quantserver.report.dto;

import com.example.quantserver.report.entity.MarketReport;
import com.example.quantserver.report.enums.ReportType;

import java.time.LocalDateTime;

public record MarketReportResponse(
        Long id,
        ReportType reportType,
        String content,
        LocalDateTime generatedAt
) {
    public static MarketReportResponse from(MarketReport report) {
        return new MarketReportResponse(
                report.getId(),
                report.getReportType(),
                report.getContent(),
                report.getGeneratedAt()
        );
    }
}