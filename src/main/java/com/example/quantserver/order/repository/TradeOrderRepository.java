package com.example.quantserver.order.repository;

import com.example.quantserver.order.entity.TradeOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long> {

    Page<TradeOrder> findByUserIdOrderByExecutedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndExecutedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    Optional<TradeOrder> findFirstByUserIdAndExecutedAtBeforeOrderByExecutedAtDesc(Long userId, LocalDateTime time);
}