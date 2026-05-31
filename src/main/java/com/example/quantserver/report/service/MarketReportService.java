package com.example.quantserver.report.service;

import com.example.quantserver.ai.client.AiServerClient;
import com.example.quantserver.ai.entity.AgentActivityLog;
import com.example.quantserver.ai.enums.AgentStatus;
import com.example.quantserver.ai.repository.AgentActivityLogRepository;
import com.example.quantserver.global.exception.BusinessException;
import com.example.quantserver.global.exception.ErrorCode;
import com.example.quantserver.report.dto.MarketReportListResponse;
import com.example.quantserver.report.dto.MarketReportResponse;
import com.example.quantserver.report.entity.MarketReport;
import com.example.quantserver.report.enums.ReportType;
import com.example.quantserver.report.repository.MarketReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarketReportService {

    private static final String AGENT_TYPE = "REPORT_GENERATOR";

    private final MarketReportRepository reportRepository;
    private final AgentActivityLogRepository logRepository;
    private final AiServerClient aiServerClient;

    public List<MarketReportListResponse> getReports() {
        return reportRepository.findAllByOrderByGeneratedAtDesc().stream()
                .map(MarketReportListResponse::from)
                .toList();
    }

    public MarketReportResponse getReport(Long id) {
        MarketReport report = reportRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));
        return MarketReportResponse.from(report);
    }

    @Transactional
    public void generateReport(ReportType reportType, LocalDateTime startedAt) {
        String content = aiServerClient.generateReport();

        reportRepository.save(MarketReport.builder()
                .reportType(reportType)
                .content(content)
                .generatedAt(LocalDateTime.now())
                .build());

        AgentActivityLog successLog = AgentActivityLog.builder()
                .agentType(AGENT_TYPE)
                .action(reportType.name())
                .status(AgentStatus.SUCCEEDED)
                .startedAt(startedAt)
                .build();
        successLog.complete(AgentStatus.SUCCEEDED, null);
        logRepository.save(successLog);
    }

    @Transactional
    public void saveFailureLog(ReportType reportType, LocalDateTime startedAt, String detail) {
        AgentActivityLog failureLog = AgentActivityLog.builder()
                .agentType(AGENT_TYPE)
                .action(reportType.name())
                .status(AgentStatus.FAILED)
                .startedAt(startedAt)
                .build();
        failureLog.complete(AgentStatus.FAILED, detail);
        logRepository.save(failureLog);
    }
}