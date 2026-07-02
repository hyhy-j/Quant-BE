package com.example.quantserver.trade.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "holdings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "stock_code", nullable = false, length = 10)
    private String stockCode;

    @Column(nullable = false)
    private Long quantity;

    @Column(name = "avg_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal avgPrice;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public BigDecimal totalValue() {
        return avgPrice.multiply(BigDecimal.valueOf(quantity));
    }
}