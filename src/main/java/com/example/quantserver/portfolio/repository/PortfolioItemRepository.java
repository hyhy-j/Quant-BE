package com.example.quantserver.portfolio.repository;

import com.example.quantserver.portfolio.entity.PortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {

    List<PortfolioItem> findByRecommendationId(Long recommendationId);
}