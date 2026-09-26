package com.example.quantserver.portfolio.entity;

import com.example.quantserver.global.common.BaseEntity;
import com.example.quantserver.investment.entity.InvestmentProfile;
import com.example.quantserver.investment.enums.ProfileType;
import com.example.quantserver.portfolio.enums.RecommendationStatus;
import com.example.quantserver.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "portfolio_recommendations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE portfolio_recommendations SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class PortfolioRecommendation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private InvestmentProfile profile;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "mdd")
    private BigDecimal mdd;

    @Column(name = "sharpe_ratio")
    private BigDecimal sharpeRatio;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecommendationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_type", length = 20)
    private ProfileType riskType;

    @Column(name = "expected_return")
    private BigDecimal expectedReturn;

    @Column(name = "backtest_curve", columnDefinition = "TEXT")
    private String backtestCurve;

    @Column(name = "monthly_returns", columnDefinition = "TEXT")
    private String monthlyReturns;

    @Column(name = "top_stocks")
    private String topStocks;

    @Column(name = "report", columnDefinition = "TEXT")
    private String report;

    @Builder
    public PortfolioRecommendation(User user, InvestmentProfile profile, BigDecimal totalAmount,
                                   BigDecimal mdd, BigDecimal sharpeRatio, ProfileType riskType,
                                   BigDecimal expectedReturn, String backtestCurve,
                                   String monthlyReturns, String topStocks, String report) {
        this.user = user;
        this.profile = profile;
        this.totalAmount = totalAmount;
        this.mdd = mdd;
        this.sharpeRatio = sharpeRatio;
        this.status = RecommendationStatus.ACTIVE;
        this.riskType = riskType;
        this.expectedReturn = expectedReturn;
        this.backtestCurve = backtestCurve;
        this.monthlyReturns = monthlyReturns;
        this.topStocks = topStocks;
        this.report = report;
    }
}