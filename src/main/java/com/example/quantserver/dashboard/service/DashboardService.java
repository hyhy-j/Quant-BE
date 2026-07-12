package com.example.quantserver.dashboard.service;

import com.example.quantserver.dashboard.dto.DashboardResponse;
import com.example.quantserver.order.dto.PnlInfo;
import com.example.quantserver.order.entity.Holding;
import com.example.quantserver.order.entity.Stock;
import com.example.quantserver.order.entity.StockPrice;
import com.example.quantserver.order.repository.HoldingRepository;
import com.example.quantserver.order.repository.StockPriceRepository;
import com.example.quantserver.order.repository.StockRepository;
import com.example.quantserver.order.service.TradeService;
import com.example.quantserver.report.dto.MarketReportResponse;
import com.example.quantserver.report.repository.MarketReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final StockRepository stockRepository;
    private final HoldingRepository holdingRepository;
    private final StockPriceRepository stockPriceRepository;
    private final MarketReportRepository marketReportRepository;
    private final TradeService tradeService;

    public DashboardResponse getDashboard(Long userId) {
        Map<String, Holding> holdingsByStockCode = holdingRepository.findAllByUserId(userId).stream()
                .collect(Collectors.toMap(Holding::getStockCode, Function.identity()));

        Map<String, BigDecimal> latestPrices = stockPriceRepository.findLatestPrices().stream()
                .collect(Collectors.toMap(StockPrice::getStockCode, StockPrice::getClose));

        List<DashboardResponse.StockSummary> stockSummaries = new ArrayList<>();

        for (Stock stock : stockRepository.findAll()) {
            BigDecimal currentPrice = latestPrices.get(stock.getCode());
            Holding holding = holdingsByStockCode.get(stock.getCode());

            if (holding == null) {
                stockSummaries.add(new DashboardResponse.StockSummary(
                        stock.getCode(), stock.getName(), currentPrice, 0L, null, null, null));
                continue;
            }

            if (currentPrice == null) {
                stockSummaries.add(new DashboardResponse.StockSummary(
                        stock.getCode(), stock.getName(), null,
                        holding.getQuantity(), holding.getAvgPrice(), null, null));
                continue;
            }

            BigDecimal totalValue = currentPrice.multiply(BigDecimal.valueOf(holding.getQuantity()));

            stockSummaries.add(new DashboardResponse.StockSummary(
                    stock.getCode(), stock.getName(), currentPrice,
                    holding.getQuantity(), holding.getAvgPrice(), totalValue,
                    calculateUnrealizedPnl(holding, totalValue)));
        }

        BigDecimal totalAssets = tradeService.calculateTotalAssets(userId);
        PnlInfo todayPnl = tradeService.getStats(userId).daily();
        PnlInfo cumulativePnl = tradeService.getCumulativePnl(userId, totalAssets);

        DashboardResponse.AssetSummary asset =
                new DashboardResponse.AssetSummary(totalAssets, todayPnl, cumulativePnl);

        return new DashboardResponse(asset, stockSummaries, getTodayReport());
    }

    private PnlInfo calculateUnrealizedPnl(Holding holding, BigDecimal totalValue) {
        BigDecimal cost = holding.getAvgPrice().multiply(BigDecimal.valueOf(holding.getQuantity()));
        BigDecimal amount = totalValue.subtract(cost);
        BigDecimal rate = cost.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : amount.divide(cost, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        return new PnlInfo(amount, rate);
    }

    private MarketReportResponse getTodayReport() {
        LocalDate today = LocalDate.now();
        return marketReportRepository
                .findFirstByGeneratedAtBetweenOrderByGeneratedAtDesc(
                        today.atStartOfDay(), today.plusDays(1).atStartOfDay())
                .map(MarketReportResponse::from)
                .orElse(null);
    }
}