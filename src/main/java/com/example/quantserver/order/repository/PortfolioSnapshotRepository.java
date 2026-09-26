package com.example.quantserver.order.repository;

import com.example.quantserver.order.entity.PortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Long> {

    List<PortfolioSnapshot> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate start, LocalDate end);

    @Modifying
    @Query(value = """
            INSERT INTO portfolio_snapshots (user_id, date, total_assets, created_at)
            VALUES (:userId, :date, :totalAssets, NOW())
            ON CONFLICT (user_id, date) DO NOTHING
            """, nativeQuery = true)
    void upsertIfAbsent(@Param("userId") Long userId, @Param("date") LocalDate date,
                         @Param("totalAssets") BigDecimal totalAssets);
}