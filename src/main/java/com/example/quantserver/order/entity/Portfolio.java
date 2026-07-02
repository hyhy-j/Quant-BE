package com.example.quantserver.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal balance;

    @Column(name = "initial_balance", nullable = false, precision = 18, scale = 4)
    private BigDecimal initialBalance;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Portfolio init(Long userId, BigDecimal initialBalance) {
        return Portfolio.builder()
                .userId(userId)
                .balance(initialBalance)
                .initialBalance(initialBalance)
                .build();
    }

    public boolean isLossHalted() {
        if (initialBalance.compareTo(BigDecimal.ZERO) == 0) return false;
        BigDecimal lossRate = balance.subtract(initialBalance)
                .divide(initialBalance, 4, RoundingMode.HALF_UP);
        return lossRate.compareTo(new BigDecimal("-0.15")) < 0;
    }
}