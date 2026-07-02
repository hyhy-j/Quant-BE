package com.example.quantserver.trade.controller;

import com.example.quantserver.trade.dto.OrderStatusCallbackRequest;
import com.example.quantserver.trade.service.TradeNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Internal", description = "내부 서버 간 통신 API")
@Slf4j
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class OrderCallbackController {

    private final TradeNotificationService tradeNotificationService;

    @Operation(summary = "주문 체결 콜백", description = "AI 서버로부터 주문 처리 결과를 수신하고 SSE로 유저에게 전달합니다.")
    @PostMapping("/orders/status")
    public void receiveOrderStatus(@RequestBody OrderStatusCallbackRequest request) {
        if ("COMPLETED".equals(request.status())) {
            log.info("주문 체결 완료 orderId={} userId={} stockId={} side={}",
                    request.orderId(), request.userId(), request.stockId(), request.side());
        } else {
            log.warn("주문 체결 실패 userId={} stockId={} side={} message={}",
                    request.userId(), request.stockId(), request.side(), request.message());
        }
        tradeNotificationService.notify(request.userId(), request);
    }
}