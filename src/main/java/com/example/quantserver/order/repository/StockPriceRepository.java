package com.example.quantserver.order.repository;

import com.example.quantserver.order.entity.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {

    Optional<StockPrice> findFirstByStockCodeOrderByDateDesc(String stockCode);

    @Query(value = """
            SELECT DISTINCT ON (stock_code) *
            FROM stock_prices
            WHERE deleted_at IS NULL
            ORDER BY stock_code, date DESC
            """, nativeQuery = true)
    List<StockPrice> findLatestPrices();
}