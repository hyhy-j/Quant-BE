package com.example.quantserver.ai.controller;

import com.example.quantserver.ai.client.AiServerClient;
import com.example.quantserver.ai.dto.AgentRequest;
import com.example.quantserver.ai.dto.AgentResponse;
import com.example.quantserver.global.jwt.CustomUserDetails;
import com.example.quantserver.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI", description = "AI 서버 통신 테스트 API (임시)")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiServerClient aiServerClient;

    @Operation(summary = "AI 서버 통신 테스트", description = "요청 데이터를 AI 서버에 전달하고 응답을 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "통신 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "AI 서버 연결 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "504", description = "AI 서버 응답 시간 초과")
    })
    @PostMapping("/test")
    public ApiResponse<AgentResponse> test(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AgentRequest request) {
        AgentRequest agentRequest = new AgentRequest(userDetails.getId(), request.riskLevel(), request.investmentAmount());
        return ApiResponse.success(aiServerClient.requestAgent(agentRequest));
    }
}