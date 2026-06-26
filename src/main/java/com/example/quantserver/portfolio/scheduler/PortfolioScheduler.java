package com.example.quantserver.portfolio.scheduler;

import com.example.quantserver.global.exception.BusinessException;
import com.example.quantserver.investment.entity.InvestmentProfile;
import com.example.quantserver.investment.repository.InvestmentProfileRepository;
import com.example.quantserver.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioScheduler {

    private static final int MAX_RETRY = 3;

    private final PortfolioService portfolioService;
    private final InvestmentProfileRepository profileRepository;

    @Scheduled(cron = "0 0 9 * * MON")
    public void generateWeeklyPortfolios() {
        List<InvestmentProfile> profiles = profileRepository.findAllCurrentWithUser();
        log.info("주간 포트폴리오 생성 시작 - 대상 유저 수={}", profiles.size());

        int success = 0;
        int failure = 0;

        for (InvestmentProfile profile : profiles) {
            if (generateWithRetry(profile)) {
                success++;
            } else {
                failure++;
            }
        }

        log.info("주간 포트폴리오 생성 완료 - 성공={} 실패={} 전체={}", success, failure, profiles.size());
    }

    private boolean generateWithRetry(InvestmentProfile profile) {
        Long userId = profile.getUser().getId();
        long delayMs = 1000;
        int totalAttempts = MAX_RETRY + 1;

        for (int attempt = 1; attempt <= totalAttempts; attempt++) {
            try {
                portfolioService.generateAndSave(profile.getUser(), profile);
                log.info("포트폴리오 생성 성공 userId={}", userId);
                return true;
            } catch (BusinessException e) {
                log.warn("포트폴리오 생성 실패 userId={} {}/{}회 - {}", userId, attempt, totalAttempts, e.getMessage());
                if (attempt < totalAttempts) {
                    sleep(delayMs);
                    delayMs *= 2;
                }
            } catch (Exception e) {
                log.error("포트폴리오 생성 중 예상치 못한 오류 발생 userId={}", userId, e);
                return false;
            }
        }

        log.error("포트폴리오 생성 최종 실패 userId={}", userId);
        return false;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}