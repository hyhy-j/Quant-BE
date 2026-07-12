package com.example.quantserver.order.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_snapshots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "total_assets", nullable = false, precision = 18, scale = 4)
    private BigDecimal totalAssets;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public PortfolioSnapshot(Long userId, LocalDate date, BigDecimal totalAssets) {
        this.userId = userId;
        this.date = date;
        this.totalAssets = totalAssets;
        this.createdAt = LocalDateTime.now();
    }
}