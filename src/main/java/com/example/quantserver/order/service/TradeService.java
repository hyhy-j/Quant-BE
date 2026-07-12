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
import com.example.quantserver.order.entity.Holding;
import com.example.quantserver.order.entity.Portfolio;
import com.example.quantserver.order.entity.PortfolioSnapshot;
import com.example.quantserver.order.entity.Stock;
import com.example.quantserver.order.entity.StockPrice;
import com.example.quantserver.order.entity.TradeOrder;
import com.example.quantserver.order.repository.HoldingRepository;
import com.example.quantserver.order.repository.PortfolioRepository;
import com.example.quantserver.order.repository.PortfolioSnapshotRepository;
import com.example.quantserver.order.repository.StockPriceRepository;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeService {

    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("10000000");
    private static final int HISTORY_DAYS = 30;

    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final TradeOrderRepository tradeOrderRepository;
    private final HoldingRepository holdingRepository;
    private final StockPriceRepository stockPriceRepository;
    private final PortfolioSnapshotRepository portfolioSnapshotRepository;
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
            return new OrderStatsResponse(zero, zero, zero, List.of());
        }

        BigDecimal currentBalance = portfolio.getBalance();
        BigDecimal initialBalance = portfolio.getInitialBalance();

        LocalDateTime dailyStart = LocalDate.now().atStartOfDay();
        LocalDateTime weeklyStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime monthlyStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        return new OrderStatsResponse(
                calculatePnl(userId, currentBalance, initialBalance, dailyStart),
                calculatePnl(userId, currentBalance, initialBalance, weeklyStart),
                calculatePnl(userId, currentBalance, initialBalance, monthlyStart),
                getHistory(userId)
        );
    }

    private List<OrderStatsResponse.HistoryPoint> getHistory(Long userId) {
        LocalDate start = LocalDate.now().minusDays(HISTORY_DAYS - 1L);
        return portfolioSnapshotRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, start, LocalDate.now())
                .stream()
                .map(snapshot -> new OrderStatsResponse.HistoryPoint(snapshot.getDate(), snapshot.getTotalAssets()))
                .toList();
    }

    public BigDecimal calculateTotalAssets(Long userId) {
        BigDecimal cashBalance = portfolioRepository.findByUserId(userId)
                .map(Portfolio::getBalance)
                .orElse(BigDecimal.ZERO);

        List<Holding> holdings = holdingRepository.findAllByUserId(userId);
        if (holdings.isEmpty()) {
            return cashBalance;
        }

        Map<String, BigDecimal> latestPrices = stockPriceRepository.findLatestPrices().stream()
                .collect(Collectors.toMap(StockPrice::getStockCode, StockPrice::getClose));

        BigDecimal holdingsValue = holdings.stream()
                .map(holding -> latestPrices.getOrDefault(holding.getStockCode(), holding.getAvgPrice())
                        .multiply(BigDecimal.valueOf(holding.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return cashBalance.add(holdingsValue);
    }

    @Transactional
    public void saveTodaySnapshot(Long userId) {
        LocalDate today = LocalDate.now();
        if (portfolioSnapshotRepository.findByUserIdAndDate(userId, today).isPresent()) {
            return;
        }

        portfolioSnapshotRepository.save(PortfolioSnapshot.builder()
                .userId(userId)
                .date(today)
                .totalAssets(calculateTotalAssets(userId))
                .build());
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