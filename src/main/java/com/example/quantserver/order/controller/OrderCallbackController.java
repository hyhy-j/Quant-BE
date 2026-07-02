package com.example.quantserver.order.controller;

import com.example.quantserver.order.dto.OrderCallbackRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/internal/orders")
public class OrderCallbackController {

    @PostMapping("/status")
    public ResponseEntity<Void> receiveCallback(@RequestBody OrderCallbackRequest request) {
        log.info("주문 콜백 수신 orderId={} userId={} status={} message={}",
                request.orderId(), request.userId(), request.status(), request.message());
        return ResponseEntity.ok().build();
    }
}