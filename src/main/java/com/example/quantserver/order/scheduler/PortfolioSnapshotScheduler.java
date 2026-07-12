package com.example.quantserver.order.scheduler;

import com.example.quantserver.order.entity.Portfolio;
import com.example.quantserver.order.repository.PortfolioRepository;
import com.example.quantserver.order.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioSnapshotScheduler {

    private final PortfolioRepository portfolioRepository;
    private final TradeService tradeService;

    @Scheduled(cron = "0 0 19 * * MON-FRI", zone = "Asia/Seoul")
    public void saveDailySnapshots() {
        List<Portfolio> portfolios = portfolioRepository.findAll();
        log.info("포트폴리오 일별 스냅샷 저장 시작 - 대상 유저 수={}", portfolios.size());

        int success = 0;
        int failure = 0;

        for (Portfolio portfolio : portfolios) {
            try {
                tradeService.saveTodaySnapshot(portfolio.getUserId());
                success++;
            } catch (Exception e) {
                failure++;
                log.error("포트폴리오 스냅샷 저장 실패 userId={}", portfolio.getUserId(), e);
            }
        }

        log.info("포트폴리오 일별 스냅샷 저장 완료 - 성공={} 실패={} 전체={}", success, failure, portfolios.size());
    }
}