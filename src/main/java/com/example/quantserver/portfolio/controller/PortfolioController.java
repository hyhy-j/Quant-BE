package com.example.quantserver.portfolio.controller;

import com.example.quantserver.global.jwt.CustomUserDetails;
import com.example.quantserver.global.response.ApiResponse;
import com.example.quantserver.portfolio.dto.PortfolioResponse;
import com.example.quantserver.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Portfolio", description = "포트폴리오 API")
@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @Operation(summary = "최신 포트폴리오 조회", description = "매주 월요일 자동 생성된 가장 최근 포트폴리오를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "포트폴리오 없음")
    })
    @GetMapping("/latest")
    public ApiResponse<PortfolioResponse> getLatest(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(portfolioService.getLatest(userDetails.getId()));
    }
}