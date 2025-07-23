package com.everbit.everbit.trade.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.everbit.everbit.trade.dto.TradeResponse;
import com.everbit.everbit.trade.repository.TradeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeService {
    private final TradeRepository tradeRepository;

    public List<TradeResponse> getTrades() {
        return TradeResponse.from(tradeRepository.findAll());
    }
}
