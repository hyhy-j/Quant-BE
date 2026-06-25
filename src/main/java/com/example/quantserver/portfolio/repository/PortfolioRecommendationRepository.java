package com.example.quantserver.portfolio.repository;

import com.example.quantserver.portfolio.entity.PortfolioRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortfolioRecommendationRepository extends JpaRepository<PortfolioRecommendation, Long> {

    Optional<PortfolioRecommendation> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}