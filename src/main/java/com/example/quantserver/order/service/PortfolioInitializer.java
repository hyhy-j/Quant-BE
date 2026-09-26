package com.example.quantserver.order.service;

import com.example.quantserver.order.entity.Portfolio;
import com.example.quantserver.order.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PortfolioInitializer {

    private final PortfolioRepository portfolioRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureExists(Long userId, BigDecimal initialBalance) {
        if (portfolioRepository.findByUserId(userId).isPresent()) return;
        try {
            portfolioRepository.save(Portfolio.init(userId, initialBalance));
        } catch (DataIntegrityViolationException ignored) {
            // 동시 첫 주문으로 다른 트랜잭션이 이미 생성한 경우 — 무시하고 진행
        }
    }
}