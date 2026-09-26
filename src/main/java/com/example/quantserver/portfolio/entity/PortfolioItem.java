package com.example.quantserver.portfolio.entity;

import com.example.quantserver.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "portfolio_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE portfolio_items SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class PortfolioItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private PortfolioRecommendation recommendation;

    @Column(name = "stock_code", nullable = false, length = 20)
    private String stockCode;

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "weight", nullable = false)
    private BigDecimal weight;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Builder
    public PortfolioItem(PortfolioRecommendation recommendation, String stockCode, String name,
                         BigDecimal weight, BigDecimal amount, Long quantity, String reason) {
        this.recommendation = recommendation;
        this.stockCode = stockCode;
        this.name = name;
        this.weight = weight;
        this.amount = amount;
        this.quantity = quantity;
        this.reason = reason;
    }
}