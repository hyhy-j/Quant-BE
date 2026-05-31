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
import java.util.Optional;

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

    public Optional<String> fetchContent() {
        return aiServerClient.generateReport();
    }

    @Transactional
    public void saveReport(String content, ReportType reportType, LocalDateTime startedAt) {
        reportRepository.save(MarketReport.builder()
                .reportType(reportType)
                .content(content)
                .generatedAt(LocalDateTime.now())
                .build());

        AgentActivityLog log = AgentActivityLog.builder()
                .agentType(AGENT_TYPE)
                .action(reportType.name())
                .status(AgentStatus.SUCCEEDED)
                .startedAt(startedAt)
                .build();
        log.complete(AgentStatus.SUCCEEDED, null);
        logRepository.save(log);
    }

    @Transactional
    public void saveFailureLog(ReportType reportType, LocalDateTime startedAt, String detail) {
        AgentActivityLog log = AgentActivityLog.builder()
                .agentType(AGENT_TYPE)
                .action(reportType.name())
                .status(AgentStatus.FAILED)
                .startedAt(startedAt)
                .build();
        log.complete(AgentStatus.FAILED, detail);
        logRepository.save(log);
    }
}