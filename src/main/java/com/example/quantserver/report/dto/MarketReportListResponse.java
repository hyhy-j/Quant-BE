package com.example.quantserver.report.dto;

import com.example.quantserver.report.entity.MarketReport;
import com.example.quantserver.report.enums.ReportType;

import java.time.LocalDateTime;

public record MarketReportListResponse(
        Long id,
        ReportType reportType,
        LocalDateTime generatedAt
) {
    public static MarketReportListResponse from(MarketReport report) {
        return new MarketReportListResponse(
                report.getId(),
                report.getReportType(),
                report.getGeneratedAt()
        );
    }
}