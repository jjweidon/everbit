package com.everbit.everbit.upbit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.everbit.everbit.global.dto.ApiResponse;
import com.everbit.everbit.upbit.dto.quotation.TickerResponse;
import com.everbit.everbit.upbit.service.UpbitQuotationClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
}
