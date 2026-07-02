package com.example.quantserver.trade.repository;

import com.example.quantserver.trade.entity.TradeOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long> {

    long countByUserIdAndExecutedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    Optional<TradeOrder> findFirstByUserIdAndExecutedAtBeforeOrderByExecutedAtDesc(Long userId, LocalDateTime time);
}