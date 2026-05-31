package com.example.quantserver.report.controller;

import com.example.quantserver.ai.dto.AgentActivityLogResponse;
import com.example.quantserver.ai.repository.AgentActivityLogRepository;
import com.example.quantserver.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin", description = "관리자 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AgentActivityLogRepository logRepository;

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
}