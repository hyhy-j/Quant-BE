package com.example.quantserver.investment.entity;

import com.example.quantserver.global.common.BaseEntity;
import com.example.quantserver.investment.enums.ProfileType;
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
@Table(name = "investment_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE investment_profiles SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class InvestmentProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String investmentGoal;

    @Column(nullable = false)
    private short riskTolerance;

    @Column(nullable = false)
    private String investmentPeriod;

    @Column(nullable = false)
    private BigDecimal investableAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileType profileType;

    @Column(name = "is_current", nullable = false)
    private boolean current;

    @Builder
    public InvestmentProfile(User user, String investmentGoal, short riskTolerance,
                              String investmentPeriod, BigDecimal investableAmount,
                              ProfileType profileType) {
        this.user = user;
        this.investmentGoal = investmentGoal;
        this.riskTolerance = riskTolerance;
        this.investmentPeriod = investmentPeriod;
        this.investableAmount = investableAmount;
        this.profileType = profileType;
        this.current = true;
    }

    public void deactivate() {
        this.current = false;
    }
}