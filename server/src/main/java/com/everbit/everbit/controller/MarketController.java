package com.everbit.everbit.controller;

import com.everbit.everbit.client.UpbitClient;
import com.everbit.everbit.dto.upbit.CandleDto;
import com.everbit.everbit.dto.upbit.TickerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 시장 데이터 관련 컨트롤러
 */
@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
@Slf4j
public class MarketController {
    
    private final UpbitClient upbitClient;
    
    /**
     * 현재 시세 정보 조회
     * 
     * @param markets 마켓 코드 목록 (쉼표로 구분)
     * @return 시세 정보 목록
     */
    @GetMapping("/ticker")
    public ResponseEntity<Flux<TickerDto>> getTicker(
            @RequestParam(value = "markets", defaultValue = "KRW-BTC") String marketsParam) {
        List<String> markets = List.of(marketsParam.split(","));
        
        return ResponseEntity.ok(upbitClient.getTicker(markets));
    }
    
    /**
     * 분 캔들 조회
     * 
     * @param market 마켓 코드
     * @param unit 분 단위 (1, 3, 5, 15, 10, 30, 60, 240)
     * @param count 캔들 개수 (최대 200)
     * @return 캔들 데이터 목록
     */
    @GetMapping("/candles/minutes")
    public ResponseEntity<Flux<CandleDto>> getMinuteCandles(
            @RequestParam(value = "market", defaultValue = "KRW-BTC") String market,
            @RequestParam(value = "unit", defaultValue = "60") String unit,
            @RequestParam(value = "count", defaultValue = "100") Integer count) {
        
        return ResponseEntity.ok(upbitClient.getMinuteCandles(market, unit, count));
    }
    
    /**
     * 일 캔들 조회
     * 
     * @param market 마켓 코드
     * @param count 캔들 개수 (최대 200)
     * @return 캔들 데이터 목록
     */
    @GetMapping("/candles/days")
    public ResponseEntity<Flux<CandleDto>> getDayCandles(
            @RequestParam(value = "market", defaultValue = "KRW-BTC") String market,
            @RequestParam(value = "count", defaultValue = "100") Integer count) {
        
        return ResponseEntity.ok(upbitClient.getDayCandles(market, count));
    }
} 