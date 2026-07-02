package com.example.quantserver.order.service;

import com.example.quantserver.ai.client.AiServerClient;
import com.example.quantserver.global.exception.BusinessException;
import com.example.quantserver.global.exception.ErrorCode;
import com.example.quantserver.order.dto.AiOrderExecuteRequest;
import com.example.quantserver.order.dto.OrderExecuteResponse;
import com.example.quantserver.order.dto.TradeOrderRequest;
import com.example.quantserver.order.entity.Portfolio;
import com.example.quantserver.order.entity.Stock;
import com.example.quantserver.order.repository.PortfolioRepository;
import com.example.quantserver.order.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class TradeService {

    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("10000000");

    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final AiServerClient aiServerClient;
    private final RiskCheckService riskCheckService;

    public OrderExecuteResponse placeOrder(Long userId, TradeOrderRequest request) {
        Stock stock = stockRepository.findByName(request.stockName())
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_FOUND));

        Portfolio portfolio = portfolioRepository.findWithLockByUserId(userId)
                .orElseGet(() -> portfolioRepository.save(Portfolio.init(userId, INITIAL_BALANCE)));

        riskCheckService.check(portfolio, stock.getCode(), request.side(), request.quantity());

        return aiServerClient.executePortfolio(new AiOrderExecuteRequest(
                userId,
                stock.getCode(),
                request.side(),
                request.quantity()
        ));
    }
}