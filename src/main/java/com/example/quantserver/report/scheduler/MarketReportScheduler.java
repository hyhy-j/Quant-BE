package com.example.quantserver.report.scheduler;

import com.example.quantserver.global.exception.BusinessException;
import com.example.quantserver.report.enums.ReportType;
import com.example.quantserver.report.service.MarketReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketReportScheduler {

    private static final int MAX_RETRY = 3;

    private final MarketReportService marketReportService;

    @Scheduled(cron = "0 0 8 * * MON-FRI")
    public void generateMorningReport() {
        generate(ReportType.MORNING);
    }

    @Scheduled(cron = "0 0 18 * * MON-FRI")
    public void generateEveningReport() {
        generate(ReportType.EVENING);
    }

    private void generate(ReportType reportType) {
        LocalDateTime startedAt = LocalDateTime.now();
        log.info("{} 리포트 생성 시작", reportType);

        int attempt = 0;
        long delayMs = 1000;
        String lastError = null;

        while (attempt <= MAX_RETRY) {
            try {
                Optional<String> content = marketReportService.fetchContent();
                if (content.isEmpty()) {
                    log.warn("{} 리포트 AI 빈 응답 수신 - 재시도 없이 실패 처리", reportType);
                    marketReportService.saveFailureLog(reportType, startedAt, "AI 서버 빈 응답");
                    return;
                }
                marketReportService.saveReport(content.get(), reportType, startedAt);
                log.info("{} 리포트 생성 완료", reportType);
                return;
            } catch (BusinessException e) {
                attempt++;
                lastError = e.getMessage();
                log.warn("{} 리포트 생성 실패 {}/{}회 - {}", reportType, attempt, MAX_RETRY, lastError);
                if (attempt <= MAX_RETRY) {
                    sleep(delayMs);
                    delayMs *= 2;
                }
            } catch (Exception e) {
                log.error("{} 리포트 처리 중 예상치 못한 오류 발생", reportType, e);
                lastError = e.getMessage();
                break;
            }
        }

        try {
            marketReportService.saveFailureLog(reportType, startedAt, lastError);
        } catch (Exception e) {
            log.error("{} 실패 로그 저장 실패", reportType, e);
        }
        log.error("{} 리포트 생성 최종 실패", reportType);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}