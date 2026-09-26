package com.example.quantserver.report.repository;

import com.example.quantserver.report.entity.MarketReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MarketReportRepository extends JpaRepository<MarketReport, Long> {

    List<MarketReport> findAllByOrderByGeneratedAtDesc();

    Optional<MarketReport> findFirstByGeneratedAtBetweenOrderByGeneratedAtDesc(
            LocalDateTime start, LocalDateTime end);
}