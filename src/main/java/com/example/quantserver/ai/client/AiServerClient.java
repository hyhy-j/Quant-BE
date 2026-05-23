package com.example.quantserver.ai.client;

import com.example.quantserver.ai.dto.AgentRequest;
import com.example.quantserver.ai.dto.AgentResponse;
import com.example.quantserver.global.exception.BusinessException;
import com.example.quantserver.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiServerClient {

    private final RestClient aiServerRestClient;

    public AgentResponse requestAgent(AgentRequest request) {
        try {
            return aiServerRestClient.post()
                    .uri("/agent/run")
                    .body(request)
                    .retrieve()
                    .body(AgentResponse.class);
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("AI 서버 응답 시간 초과", e);
                throw new BusinessException(ErrorCode.AI_SERVER_TIMEOUT);
            }
            log.error("AI 서버 연결 실패", e);
            throw new BusinessException(ErrorCode.AI_SERVER_UNAVAILABLE);
        } catch (RestClientResponseException e) {
            log.error("AI 서버 오류 응답 status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.AI_SERVER_UNAVAILABLE);
        }
    }
}