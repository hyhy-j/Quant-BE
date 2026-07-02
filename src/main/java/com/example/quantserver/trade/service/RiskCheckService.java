package com.example.quantserver.trade.service;

import com.example.quantserver.global.exception.BusinessException;
import com.example.quantserver.global.exception.ErrorCode;
import com.example.quantserver.trade.entity.Holding;
import com.example.quantserver.trade.entity.Portfolio;
import com.example.quantserver.trade.enums.OrderSide;
import com.example.quantserver.trade.repository.HoldingRepository;
import com.example.quantserver.trade.repository.TradeOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiskCheckService {

    private static final int DAILY_TRADE_LIMIT = 10;
    private static final BigDecimal CONCENTRATION_LIMIT = new BigDecimal("0.20");

    private final TradeOrderRepository tradeOrderRepository;
    private final HoldingRepository holdingRepository;

    public void check(Portfolio portfolio, String stockCode, OrderSide side, long quantity) {
        checkLossHalt(portfolio);
        checkDailyTradeLimit(portfolio.getUserId());
        if (side == OrderSide.BUY) {
            checkConcentrationLimit(portfolio, stockCode, quantity);
        }
    }

    private void checkLossHalt(Portfolio portfolio) {
        if (portfolio.isLossHalted()) {
            throw new BusinessException(ErrorCode.RISK_TRADING_HALTED);
        }
    }

    private void checkDailyTradeLimit(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long count = tradeOrderRepository.countByUserIdAndExecutedAtBetween(userId, startOfDay, endOfDay);
        if (count >= DAILY_TRADE_LIMIT) {
            throw new BusinessException(ErrorCode.RISK_DAILY_TRADE_LIMIT);
        }
    }

    private void checkConcentrationLimit(Portfolio portfolio, String stockCode, long quantity) {
        List<Holding> holdings = holdingRepository.findAllByUserId(portfolio.getUserId());

        Optional<Holding> targetHolding = holdings.stream()
                .filter(h -> h.getStockCode().equals(stockCode))
                .findFirst();

        // 기존 보유 내역이 없으면 매입 단가를 알 수 없어 비중 계산 불가 → 검사 생략
        if (targetHolding.isEmpty()) return;

        BigDecimal holdingsValue = holdings.stream()
                .map(Holding::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPortfolioValue = portfolio.getBalance().add(holdingsValue);

        if (totalPortfolioValue.compareTo(BigDecimal.ZERO) == 0) return;

        Holding holding = targetHolding.get();
        BigDecimal newStockValue = holding.getAvgPrice()
                .multiply(BigDecimal.valueOf(holding.getQuantity() + quantity));
        BigDecimal weight = newStockValue.divide(totalPortfolioValue, 4, RoundingMode.HALF_UP);

        if (weight.compareTo(CONCENTRATION_LIMIT) > 0) {
            throw new BusinessException(ErrorCode.RISK_CONCENTRATION_LIMIT);
        }
    }
}