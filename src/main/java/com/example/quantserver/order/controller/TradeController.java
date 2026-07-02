package com.example.quantserver.order.controller;

import com.example.quantserver.global.jwt.CustomUserDetails;
import com.example.quantserver.global.response.ApiResponse;
import com.example.quantserver.order.dto.OrderExecuteResponse;
import com.example.quantserver.order.dto.OrderStatsResponse;
import com.example.quantserver.order.dto.TradeOrderRequest;
import com.example.quantserver.order.dto.TradeOrderResponse;
import com.example.quantserver.order.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Order", description = "가상 매매 API")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @Operation(summary = "주문 실행", description = "리스크 검사 후 AI 서버에 매수/매도 주문을 요청합니다.")
    @PostMapping
    public ApiResponse<OrderExecuteResponse> placeOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TradeOrderRequest request
    ) {
        return ApiResponse.success(tradeService.placeOrder(userDetails.getId(), request));
    }

    @Operation(summary = "거래 내역 조회", description = "전체 거래 내역을 최신순으로 조회합니다.")
    @GetMapping
    public ApiResponse<Page<TradeOrderResponse>> getOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "executedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.success(tradeService.getOrders(userDetails.getId(), pageable));
    }

    @Operation(summary = "수익률 통계 조회", description = "일간·주간·월간 수익률을 조회합니다.")
    @GetMapping("/stats")
    public ApiResponse<OrderStatsResponse> getStats(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(tradeService.getStats(userDetails.getId()));
    }
}