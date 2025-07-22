package com.everbit.everbit.upbit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.everbit.everbit.global.dto.ApiResponse;
import com.everbit.everbit.upbit.dto.quotation.TickerResponse;
import com.everbit.everbit.upbit.dto.quotation.MinuteCandleResponse;
import com.everbit.everbit.upbit.dto.quotation.DayCandleResponse;
import com.everbit.everbit.upbit.dto.quotation.WeekCandleResponse;
import com.everbit.everbit.upbit.dto.quotation.MonthCandleResponse;
import com.everbit.everbit.upbit.dto.quotation.SecondCandleResponse;
import com.everbit.everbit.upbit.service.UpbitQuotationClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upbit")
public class UpbitQuotationController {
    private final UpbitQuotationClient upbitClient;

    // 종목 단위 현재가 정보 조회
    @GetMapping("/ticker")
    public ApiResponse<List<TickerResponse>> getTickers(
            @RequestParam(name = "markets") List<String> markets) {
        log.info("GET 종목 단위 현재가 정보 조회 - markets: {}", markets);
        List<TickerResponse> response = upbitClient.getTickers(markets);
        return ApiResponse.success(response, "종목 단위 현재가 정보 조회 성공");
    }

    // 마켓 단위 현재가 정보 조회
    @GetMapping("/ticker/all")
    public ApiResponse<List<TickerResponse>> getAllTickers(
            @RequestParam(name = "quote_currencies", required = false) List<String> quoteCurrencies) {
        log.info("GET 마켓 단위 현재가 정보 조회 - quote_currencies: {}", quoteCurrencies);
        List<TickerResponse> response = upbitClient.getAllTickers(quoteCurrencies);
        return ApiResponse.success(response, "마켓 단위 현재가 정보 조회 성공");
    }

    // 초 캔들 조회
    @GetMapping("/candles/seconds")
    public ApiResponse<List<SecondCandleResponse>> getSecondCandles(
            @RequestParam String market,
            @RequestParam(required = false) OffsetDateTime to,
            @RequestParam(required = false) Integer count) {
        log.info("GET 초 캔들 조회 - market: {}, to: {}, count: {}", market, to, count);
        List<SecondCandleResponse> response = upbitClient.getSecondCandles(market, to, count);
        return ApiResponse.success(response, "초 캔들 조회 성공");
    }

    // 분 캔들 조회
    @GetMapping("/candles/minutes/{unit}")
    public ApiResponse<List<MinuteCandleResponse>> getMinuteCandles(
            @PathVariable int unit,
            @RequestParam String market,
            @RequestParam(required = false) OffsetDateTime to,
            @RequestParam(required = false) Integer count) {
        log.info("GET 분 캔들 조회 - unit: {}, market: {}, to: {}, count: {}", unit, market, to, count);
        List<MinuteCandleResponse> response = upbitClient.getMinuteCandles(unit, market, to, count);
        return ApiResponse.success(response, "분 캔들 조회 성공");
    }

    // 일 캔들 조회
    @GetMapping("/candles/days")
    public ApiResponse<List<DayCandleResponse>> getDayCandles(
            @RequestParam String market,
            @RequestParam(required = false) OffsetDateTime to,
            @RequestParam(required = false) Integer count,
            @RequestParam(name = "converting_price_unit", required = false) String convertingPriceUnit) {
        log.info("GET 일 캔들 조회 - market: {}, to: {}, count: {}, convertingPriceUnit: {}", 
                market, to, count, convertingPriceUnit);
        List<DayCandleResponse> response = upbitClient.getDayCandles(market, to, count, convertingPriceUnit);
        return ApiResponse.success(response, "일 캔들 조회 성공");
    }

    // 주 캔들 조회
    @GetMapping("/candles/weeks")
    public ApiResponse<List<WeekCandleResponse>> getWeekCandles(
            @RequestParam String market,
            @RequestParam(required = false) OffsetDateTime to,
            @RequestParam(required = false) Integer count) {
        log.info("GET 주 캔들 조회 - market: {}, to: {}, count: {}", market, to, count);
        List<WeekCandleResponse> response = upbitClient.getWeekCandles(market, to, count);
        return ApiResponse.success(response, "주 캔들 조회 성공");
    }

    // 월 캔들 조회
    @GetMapping("/candles/months")
    public ApiResponse<List<MonthCandleResponse>> getMonthCandles(
            @RequestParam String market,
            @RequestParam(required = false) OffsetDateTime to,
            @RequestParam(required = false) Integer count) {
        log.info("GET 월 캔들 조회 - market: {}, to: {}, count: {}", market, to, count);
        List<MonthCandleResponse> response = upbitClient.getMonthCandles(market, to, count);
        return ApiResponse.success(response, "월 캔들 조회 성공");
    }
}
