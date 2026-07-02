package com.example.quantserver.order.repository;

import com.example.quantserver.order.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByName(String name);
}