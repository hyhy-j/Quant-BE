package com.example.quantserver.report.repository;

import com.example.quantserver.report.entity.MarketReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketReportRepository extends JpaRepository<MarketReport, Long> {

    List<MarketReport> findAllByOrderByGeneratedAtDesc();
}