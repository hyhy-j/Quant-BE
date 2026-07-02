package com.example.quantserver.order.service;

import com.example.quantserver.global.exception.BusinessException;
import com.example.quantserver.global.exception.ErrorCode;
import com.example.quantserver.order.entity.Holding;
import com.example.quantserver.order.entity.Portfolio;
import com.example.quantserver.order.enums.OrderSide;
import com.example.quantserver.order.repository.HoldingRepository;
import com.example.quantserver.order.repository.TradeOrderRepository;
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

    public void check(Portfolio portfolio, String stockCode, OrderSide side, long quantity, BigDecimal orderAmount) {
        checkLossHalt(portfolio);
        checkDailyTradeLimit(portfolio.getUserId());
        if (side == OrderSide.BUY) {
            checkConcentrationLimit(portfolio, stockCode, quantity, orderAmount);
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

    private void checkConcentrationLimit(Portfolio portfolio, String stockCode, long quantity, BigDecimal orderAmount) {
        List<Holding> holdings = holdingRepository.findAllByUserId(portfolio.getUserId());

        Optional<Holding> targetHolding = holdings.stream()
                .filter(h -> h.getStockCode().equals(stockCode))
                .findFirst();

        BigDecimal holdingsValue = holdings.stream()
                .map(Holding::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPortfolioValue = portfolio.getBalance().add(holdingsValue);

        if (totalPortfolioValue.compareTo(BigDecimal.ZERO) == 0) return;

        BigDecimal newStockValue = targetHolding
                .map(h -> h.getAvgPrice().multiply(BigDecimal.valueOf(h.getQuantity() + quantity)))
                .orElse(orderAmount);

        BigDecimal weight = newStockValue.divide(totalPortfolioValue, 4, RoundingMode.HALF_UP);

        if (weight.compareTo(CONCENTRATION_LIMIT) > 0) {
            throw new BusinessException(ErrorCode.RISK_CONCENTRATION_LIMIT);
        }
    }
}