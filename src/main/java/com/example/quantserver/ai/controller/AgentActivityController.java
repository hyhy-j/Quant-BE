package com.example.quantserver.ai.controller;

import com.example.quantserver.ai.dto.AgentActivityLogResponse;
import com.example.quantserver.ai.repository.AgentActivityLogRepository;
import com.example.quantserver.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Agent Activity", description = "에이전트 활동 로그 API")
@RestController
@RequestMapping("/api/agent-logs")
@RequiredArgsConstructor
public class AgentActivityController {

    private final AgentActivityLogRepository logRepository;

    @Operation(summary = "최근 에이전트 활동 로그 조회", description = "리포트/포트폴리오 생성 등 에이전트 활동 로그 최신 20건을 조회합니다.")
    @GetMapping
    public ApiResponse<List<AgentActivityLogResponse>> getRecentLogs() {
        List<AgentActivityLogResponse> logs = logRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(AgentActivityLogResponse::from)
                .toList();
        return ApiResponse.success(logs);
    }
}
