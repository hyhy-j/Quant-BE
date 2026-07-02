package com.example.quantserver.portfolio.service;

import com.example.quantserver.ai.client.AiServerClient;
import com.example.quantserver.global.exception.BusinessException;
import com.example.quantserver.global.exception.ErrorCode;
import com.example.quantserver.investment.entity.InvestmentProfile;
import com.example.quantserver.investment.enums.InvestmentPeriod;
import com.example.quantserver.investment.enums.ProfileType;
import com.example.quantserver.portfolio.dto.PortfolioResponse;
import com.example.quantserver.portfolio.dto.PythonPortfolioRequest;
import com.example.quantserver.portfolio.dto.PythonPortfolioResponse;
import com.example.quantserver.portfolio.entity.PortfolioItem;
import com.example.quantserver.portfolio.entity.PortfolioRecommendation;
import com.example.quantserver.portfolio.repository.PortfolioItemRepository;
import com.example.quantserver.portfolio.repository.PortfolioRecommendationRepository;
import com.example.quantserver.user.entity.User;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private final AiServerClient aiServerClient;
    private final PortfolioRecommendationRepository recommendationRepository;
    private final PortfolioItemRepository itemRepository;
    private final JsonMapper jsonMapper;

    @Transactional
    public void generateAndSave(User user, InvestmentProfile profile) {
        PythonPortfolioRequest request = new PythonPortfolioRequest(
                user.getId(),
                profile.getInvestmentGoal(),
                profile.getRiskTolerance(),
                toPythonPeriod(profile.getInvestmentPeriod()),
                profile.getInvestableAmount(),
                profile.getProfileType().name()
        );

        PythonPortfolioResponse response = aiServerClient.requestPortfolioRecommend(request);
        validateResponse(response);

        ProfileType riskType;
        try {
            riskType = ProfileType.valueOf(response.riskType());
        } catch (IllegalArgumentException e) {
            log.error("AI 서버 응답의 riskType 값이 올바르지 않음: {}", response.riskType());
            throw new BusinessException(ErrorCode.AI_INVALID_RESPONSE);
        }

        PortfolioRecommendation recommendation = PortfolioRecommendation.builder()
                .user(user)
                .profile(profile)
                .totalAmount(profile.getInvestableAmount())
                .mdd(BigDecimal.valueOf(response.backtestResult().mdd()))
                .sharpeRatio(BigDecimal.valueOf(response.backtestResult().sharpe()))
                .riskType(riskType)
                .expectedReturn(BigDecimal.valueOf(response.backtestResult().expectedReturn()))
                .backtestCurve(serialize(response.backtestResult().curve()))
                .monthlyReturns(serialize(response.backtestResult().monthlyReturns()))
                .topStocks(serialize(response.backtestResult().topStocks()))
                .report(response.report())
                .build();

        recommendationRepository.save(recommendation);

        response.portfolio().forEach((code, detail) ->
                itemRepository.save(PortfolioItem.builder()
                        .recommendation(recommendation)
                        .stockCode(code)
                        .name(detail.name())
                        .weight(BigDecimal.valueOf(detail.weight()))
                        .amount(BigDecimal.valueOf(detail.amount()))
                        .quantity((long) detail.quantity())
                        .reason(detail.reason())
                        .build())
        );
    }

    public PortfolioResponse getLatest(Long userId) {
        PortfolioRecommendation rec = recommendationRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));

        List<PortfolioItem> items = itemRepository.findByRecommendationId(rec.getId());
        List<String> topStocks = deserializeStringList(rec.getTopStocks());
        List<Double> curve = deserializeDoubleList(rec.getBacktestCurve());
        List<Double> monthlyReturns = deserializeDoubleList(rec.getMonthlyReturns());

        return PortfolioResponse.of(rec, items, topStocks, curve, monthlyReturns);
    }

    private void validateResponse(PythonPortfolioResponse response) {
        if (response.backtestResult() == null || response.portfolio() == null || response.riskType() == null) {
            log.error("AI 서버 응답 필수 필드 누락: backtestResult={} portfolio={} riskType={}",
                    response.backtestResult(), response.portfolio(), response.riskType());
            throw new BusinessException(ErrorCode.AI_INVALID_RESPONSE);
        }
    }

    private String toPythonPeriod(InvestmentPeriod period) {
        return switch (period) {
            case UNDER_1Y -> "UNDER_1Y";
            case ONE_TO_3Y -> "1Y_TO_3Y";
            case THREE_TO_5Y -> "3Y_TO_5Y";
            case OVER_5Y -> "OVER_5Y";
        };
    }

    private String serialize(Object value) {
        try {
            return jsonMapper.writeValueAsString(value);
        } catch (JacksonException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private List<String> deserializeStringList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return jsonMapper.readValue(json, new TypeReference<>() {});
        } catch (JacksonException e) {
            log.warn("역직렬화 실패 json={}", json, e);
            return List.of();
        }
    }

    private List<Double> deserializeDoubleList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return jsonMapper.readValue(json, new TypeReference<>() {});
        } catch (JacksonException e) {
            log.warn("역직렬화 실패 json={}", json, e);
            return List.of();
        }
    }
}