package com.example.quantserver.order.repository;

import com.example.quantserver.order.entity.PortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Long> {

    Optional<PortfolioSnapshot> findByUserIdAndDate(Long userId, LocalDate date);

    List<PortfolioSnapshot> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate start, LocalDate end);
}