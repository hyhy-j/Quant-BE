package com.example.quantserver.trade.service;

import com.example.quantserver.trade.dto.OrderStatusCallbackRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TradeNotificationService {

    private static final long SSE_TIMEOUT = 300_000L;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));
        log.info("SSE 구독 등록 userId={}", userId);
        return emitter;
    }

    public void notify(Long userId, OrderStatusCallbackRequest payload) {
        SseEmitter emitter = emitters.remove(userId);
        if (emitter == null) {
            log.warn("SSE 구독자 없음 userId={}", userId);
            return;
        }
        try {
            emitter.send(SseEmitter.event()
                    .name("orderStatus")
                    .data(payload));
            emitter.complete();
        } catch (IOException e) {
            log.warn("SSE 전송 실패 userId={}", userId, e);
        }
    }
}