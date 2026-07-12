package com.example.quantserver.dashboard.controller;

import com.example.quantserver.dashboard.dto.DashboardResponse;
import com.example.quantserver.dashboard.service.DashboardService;
import com.example.quantserver.global.jwt.CustomUserDetails;
import com.example.quantserver.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "메인 대시보드 API")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "메인 대시보드 조회", description = "총 자산, 종목별 보유 현황, 오늘의 리포트를 한 번에 조회합니다.")
    @GetMapping
    public ApiResponse<DashboardResponse> getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(dashboardService.getDashboard(userDetails.getId()));
    }
}