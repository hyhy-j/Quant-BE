package com.example.quantserver.order.service;

import com.example.quantserver.ai.client.AiServerClient;
import com.example.quantserver.global.exception.BusinessException;
import com.example.quantserver.global.exception.ErrorCode;
import com.example.quantserver.order.dto.AiOrderExecuteRequest;
import com.example.quantserver.order.dto.OrderExecuteResponse;
import com.example.quantserver.order.dto.OrderStatsResponse;
import com.example.quantserver.order.dto.PnlInfo;
import com.example.quantserver.order.dto.TradeOrderRequest;
import com.example.quantserver.order.dto.TradeOrderResponse;
import com.example.quantserver.order.entity.Portfolio;
import com.example.quantserver.order.entity.Stock;
import com.example.quantserver.order.entity.TradeOrder;
import com.example.quantserver.order.repository.PortfolioRepository;
import com.example.quantserver.order.repository.StockRepository;
import com.example.quantserver.order.repository.TradeOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeService {

    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("10000000");

    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final TradeOrderRepository tradeOrderRepository;
    private final AiServerClient aiServerClient;
    private final RiskCheckService riskCheckService;
    private final PortfolioInitializer portfolioInitializer;

    @Transactional
    public OrderExecuteResponse placeOrder(Long userId, TradeOrderRequest request) {
        Stock stock = stockRepository.findByName(request.stockName())
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_FOUND));

        portfolioInitializer.ensureExists(userId, INITIAL_BALANCE);
        Portfolio portfolio = portfolioRepository.findWithLockByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));

        riskCheckService.check(portfolio, stock.getCode(), request.side(), request.quantity(), request.orderAmount());

        return aiServerClient.executePortfolio(new AiOrderExecuteRequest(
                userId,
                stock.getCode(),
                request.side(),
                request.quantity()
        ));
    }

    public Page<TradeOrderResponse> getOrders(Long userId, Pageable pageable) {
        return tradeOrderRepository.findByUserIdOrderByExecutedAtDesc(userId, pageable)
                .map(TradeOrderResponse::from);
    }

    public OrderStatsResponse getStats(Long userId) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId).orElse(null);
        if (portfolio == null) {
            PnlInfo zero = new PnlInfo(BigDecimal.ZERO, BigDecimal.ZERO);
            return new OrderStatsResponse(zero, zero, zero);
        }

        BigDecimal currentBalance = portfolio.getBalance();
        BigDecimal initialBalance = portfolio.getInitialBalance();

        LocalDateTime dailyStart = LocalDate.now().atStartOfDay();
        LocalDateTime weeklyStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime monthlyStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        return new OrderStatsResponse(
                calculatePnl(userId, currentBalance, initialBalance, dailyStart),
                calculatePnl(userId, currentBalance, initialBalance, weeklyStart),
                calculatePnl(userId, currentBalance, initialBalance, monthlyStart)
        );
    }

    public PnlInfo getCumulativePnl(Long userId, BigDecimal currentTotalAssets) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId).orElse(null);
        if (portfolio == null) {
            return new PnlInfo(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal initialBalance = portfolio.getInitialBalance();
        BigDecimal amount = currentTotalAssets.subtract(initialBalance);
        BigDecimal rate = initialBalance.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : amount.divide(initialBalance, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));

        return new PnlInfo(amount, rate);
    }

    private PnlInfo calculatePnl(Long userId, BigDecimal currentBalance,
                                  BigDecimal initialBalance, LocalDateTime periodStart) {
        BigDecimal startBalance = tradeOrderRepository
                .findFirstByUserIdAndExecutedAtBeforeOrderByExecutedAtDesc(userId, periodStart)
                .map(TradeOrder::getBalanceAfter)
                .orElse(initialBalance);

        BigDecimal amount = currentBalance.subtract(startBalance);
        BigDecimal rate = initialBalance.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : amount.divide(initialBalance, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));

        return new PnlInfo(amount, rate);
    }
}