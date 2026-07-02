package com.example.quantserver.trade.controller;

import com.example.quantserver.global.jwt.CustomUserDetails;
import com.example.quantserver.global.response.ApiResponse;
import com.example.quantserver.trade.dto.OrderExecuteResponse;
import com.example.quantserver.trade.dto.TradeOrderRequest;
import com.example.quantserver.trade.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}