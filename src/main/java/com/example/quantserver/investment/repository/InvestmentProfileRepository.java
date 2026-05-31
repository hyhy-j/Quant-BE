package com.example.quantserver.investment.repository;

import com.example.quantserver.investment.entity.InvestmentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvestmentProfileRepository extends JpaRepository<InvestmentProfile, Long> {

    Optional<InvestmentProfile> findByUserIdAndCurrentTrue(Long userId);

    boolean existsByUserIdAndCurrentTrue(Long userId);
}