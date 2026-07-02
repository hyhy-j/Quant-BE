package com.example.quantserver.ai.client;

import com.example.quantserver.ai.dto.AgentRequest;
import com.example.quantserver.ai.dto.AgentResponse;
import com.example.quantserver.ai.dto.ReportGenerateResponse;
import com.example.quantserver.global.exception.BusinessException;
import com.example.quantserver.global.exception.ErrorCode;
import com.example.quantserver.portfolio.dto.PythonPortfolioRequest;
import com.example.quantserver.portfolio.dto.PythonPortfolioResponse;
import com.example.quantserver.order.dto.AiOrderExecuteRequest;
import com.example.quantserver.order.dto.OrderExecuteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
import java.util.Optional;

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

    public PythonPortfolioResponse requestPortfolioRecommend(PythonPortfolioRequest request) {
        try {
            PythonPortfolioResponse response = aiServerRestClient.post()
                    .uri("/api/portfolio/recommend")
                    .body(request)
                    .retrieve()
                    .body(PythonPortfolioResponse.class);

            if (response == null) {
                log.warn("AI 서버 포트폴리오 빈 응답 수신 userId={}", request.userId());
                throw new BusinessException(ErrorCode.AI_SERVER_UNAVAILABLE);
            }
            return response;
        } catch (BusinessException e) {
            throw e;
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("AI 서버 응답 시간 초과 userId={}", request.userId(), e);
                throw new BusinessException(ErrorCode.AI_SERVER_TIMEOUT);
            }
            log.error("AI 서버 연결 실패 userId={}", request.userId(), e);
            throw new BusinessException(ErrorCode.AI_SERVER_UNAVAILABLE);
        } catch (RestClientResponseException e) {
            log.error("AI 서버 오류 응답 status={} body={} userId={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), request.userId());
            throw new BusinessException(ErrorCode.AI_SERVER_UNAVAILABLE);
        }
    }

    public OrderExecuteResponse executePortfolio(AiOrderExecuteRequest request) {
        try {
            OrderExecuteResponse response = aiServerRestClient.post()
                    .uri("/api/portfolio/execute")
                    .body(request)
                    .retrieve()
                    .body(OrderExecuteResponse.class);
            if (response == null) {
                log.error("AI 서버 주문 실행 빈 응답 userId={} stockId={}", request.userId(), request.stockId());
                throw new BusinessException(ErrorCode.AI_INVALID_RESPONSE);
            }
            return response;
        } catch (BusinessException e) {
            throw e;
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("AI 서버 응답 시간 초과 userId={} stockId={}", request.userId(), request.stockId(), e);
                throw new BusinessException(ErrorCode.AI_SERVER_TIMEOUT);
            }
            log.error("AI 서버 연결 실패 userId={} stockId={}", request.userId(), request.stockId(), e);
            throw new BusinessException(ErrorCode.AI_SERVER_UNAVAILABLE);
        } catch (RestClientResponseException e) {
            log.error("AI 서버 오류 응답 status={} body={} userId={} stockId={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), request.userId(), request.stockId());
            throw new BusinessException(ErrorCode.AI_SERVER_UNAVAILABLE);
        }
    }

    public Optional<String> generateReport() {
        try {
            ReportGenerateResponse response = aiServerRestClient.post()
                    .uri("/report/generate")
                    .retrieve()
                    .body(ReportGenerateResponse.class);

            if (response == null || response.portfolioReason() == null || response.portfolioReason().isBlank()) {
                log.warn("AI 서버 빈 응답 수신");
                return Optional.empty();
            }
            return Optional.of(response.portfolioReason());
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