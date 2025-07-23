package com.everbit.everbit.trade.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.everbit.everbit.trade.dto.TradeResponse;
import com.everbit.everbit.trade.service.TradeService;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trades")
public class TradeController {
    private final TradeService tradeService;

    @GetMapping
    public ResponseEntity<List<TradeResponse>> getTrades() {
        return ResponseEntity.ok(tradeService.getTrades());
    }
}
