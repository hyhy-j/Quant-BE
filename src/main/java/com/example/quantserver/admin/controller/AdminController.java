package com.example.quantserver.admin.controller;

import com.example.quantserver.ai.dto.AgentActivityLogResponse;
import com.example.quantserver.ai.repository.AgentActivityLogRepository;
import com.example.quantserver.global.response.ApiResponse;
import com.example.quantserver.report.enums.ReportType;
import com.example.quantserver.report.service.MarketReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Tag(name = "Admin", description = "관리자 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AgentActivityLogRepository logRepository;
    private final MarketReportService marketReportService;

    @Operation(summary = "에이전트 수집 현황 조회", description = "agentType 파라미터로 필터링 가능합니다.")
    @GetMapping("/data-status")
    public ApiResponse<List<AgentActivityLogResponse>> getDataStatus(
            @RequestParam(required = false) String agentType) {
        List<AgentActivityLogResponse> logs = agentType != null
                ? logRepository.findAllByAgentTypeOrderByCreatedAtDesc(agentType).stream()
                        .map(AgentActivityLogResponse::from).toList()
                : logRepository.findAllByOrderByCreatedAtDesc().stream()
                        .map(AgentActivityLogResponse::from).toList();
        return ApiResponse.success(logs);
    }

    @Operation(summary = "[테스트] 리포트 수동 생성", description = "스케줄러를 수동으로 트리거합니다. reportType: MORNING / EVENING")
    @PostMapping("/reports/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> generateReport(@RequestParam ReportType reportType) {
        LocalDateTime startedAt = LocalDateTime.now();
        Optional<String> content = marketReportService.fetchContent();
        if (content.isEmpty()) {
            marketReportService.saveFailureLog(reportType, startedAt, "AI 서버 빈 응답");
        } else {
            marketReportService.saveReport(content.get(), reportType, startedAt);
        }
        return ApiResponse.success();
    }
}