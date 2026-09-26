package com.example.quantserver.investment.repository;

import com.example.quantserver.investment.entity.InvestmentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InvestmentProfileRepository extends JpaRepository<InvestmentProfile, Long> {

    Optional<InvestmentProfile> findByUserIdAndCurrentTrue(Long userId);

    boolean existsByUserIdAndCurrentTrue(Long userId);

    @Query("SELECT p FROM InvestmentProfile p JOIN FETCH p.user WHERE p.current = true")
    List<InvestmentProfile> findAllCurrentWithUser();
}