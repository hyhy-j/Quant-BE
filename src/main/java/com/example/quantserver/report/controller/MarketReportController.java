package com.example.quantserver.report.controller;

import com.example.quantserver.global.response.ApiResponse;
import com.example.quantserver.report.dto.MarketReportListResponse;
import com.example.quantserver.report.dto.MarketReportResponse;
import com.example.quantserver.report.service.MarketReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Market Report", description = "시장 리포트 API")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class MarketReportController {

    private final MarketReportService marketReportService;

    @Operation(summary = "최신 리포트 목록 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ApiResponse<List<MarketReportListResponse>> getReports() {
        return ApiResponse.success(marketReportService.getReports());
    }

    @Operation(summary = "리포트 상세 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리포트 없음")
    })
    @GetMapping("/{id}")
    public ApiResponse<MarketReportResponse> getReport(@PathVariable Long id) {
        return ApiResponse.success(marketReportService.getReport(id));
    }
}