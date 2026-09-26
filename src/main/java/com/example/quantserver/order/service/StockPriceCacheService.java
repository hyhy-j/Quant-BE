package com.example.quantserver.order.service;

import com.example.quantserver.order.entity.Stock;
import com.example.quantserver.order.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * quant-ai-server의 price_collector가 리포트/포트폴리오 생성 파이프라인에서
 * 종목 현재가를 "price:{code}" 키로 Redis에 저장한다. quant-server는 별도의
 * 가격 수집기가 없으므로 이 캐시를 유일한 현재가 소스로 사용한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockPriceCacheService {

    private static final String PRICE_KEY_PREFIX = "price:";

    private final RedisTemplate<String, String> redisTemplate;
    private final StockRepository stockRepository;
    private final JsonMapper jsonMapper;

    public Map<String, BigDecimal> getLatestPrices() {
        List<Stock> stocks = stockRepository.findAll();
        Map<String, BigDecimal> prices = new HashMap<>();
        for (Stock stock : stocks) {
            BigDecimal price = getPrice(stock.getCode());
            if (price != null) {
                prices.put(stock.getCode(), price);
            }
        }
        return prices;
    }

    public BigDecimal getPrice(String stockCode) {
        String raw = redisTemplate.opsForValue().get(PRICE_KEY_PREFIX + stockCode);
        if (raw == null) {
            return null;
        }
        try {
            CachedPrice cached = jsonMapper.readValue(raw, CachedPrice.class);
            return cached.price() != null ? BigDecimal.valueOf(cached.price()) : null;
        } catch (JacksonException e) {
            log.warn("가격 캐시 파싱 실패 stockCode={} raw={}", stockCode, raw, e);
            return null;
        }
    }

    private record CachedPrice(Long price, String updatedAt) {
    }
}
