package com.example.quantserver.trade.controller;

import com.example.quantserver.global.jwt.CustomUserDetails;
import com.example.quantserver.global.response.ApiResponse;
import com.example.quantserver.trade.dto.TradeOrderRequest;
import com.example.quantserver.trade.service.TradeNotificationService;
import com.example.quantserver.trade.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Trade", description = "가상 매매 API")
@RestController
@RequestMapping("/api/trade")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;
    private final TradeNotificationService tradeNotificationService;

    @Operation(summary = "주문 실행", description = "리스크 검사 후 AI 서버에 매수/매도 주문을 요청합니다.")
    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<Void> placeOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TradeOrderRequest request
    ) {
        tradeService.placeOrder(userDetails.getId(), request);
        return ApiResponse.success();
    }

    @Operation(summary = "주문 결과 구독", description = "SSE로 주문 체결 결과를 실시간으로 수신합니다.")
    @GetMapping(value = "/orders/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return tradeNotificationService.subscribe(userDetails.getId());
    }
}