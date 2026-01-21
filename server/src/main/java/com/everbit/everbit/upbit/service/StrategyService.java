package com.everbit.everbit.upbit.service;

import com.everbit.everbit.trade.entity.enums.Strategy;
import com.everbit.everbit.upbit.dto.trading.TradingSignal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyService {

    /**
     * 전략에 따라 매수 시그널 여부를 판단합니다.
     */
    public boolean determineBuySignal(TradingSignal signal, Strategy strategy) {
        if (strategy == Strategy.EXTREME_FLIP) {
            return signal.dropNFlipBuySignal();
        }
        return false;
    }

    /**
     * 전략에 따라 매도 시그널 여부를 판단합니다.
     */
    public boolean determineSellSignal(TradingSignal signal, Strategy strategy) {
        if (strategy == Strategy.EXTREME_FLIP) {
            return signal.popNFlipSellSignal();
        }
        return false;
    }

    /**
     * 시그널 강도에 따른 주문 금액 계산
     */
    public BigDecimal calculateOrderAmountBySignalStrength(double signalStrength, BigDecimal baseOrderAmount, BigDecimal maxOrderAmount) {
        
        // 시그널 강도에 따른 주문 금액 계산
        BigDecimal strengthMultiplier = BigDecimal.valueOf(signalStrength);
        BigDecimal orderAmount = baseOrderAmount.add(
            maxOrderAmount.subtract(baseOrderAmount).multiply(strengthMultiplier)
        );
        
        return orderAmount.setScale(8, RoundingMode.HALF_UP);
    }

    /**
     * 선택된 전략의 시그널 강도를 계산합니다 (0.0 ~ 1.0)
     */
    public double calculateSignalStrength(TradingSignal signal, Strategy strategy) {
        if (strategy == Strategy.EXTREME_FLIP) {
            // 매수 시그널인지 매도 시그널인지 확인
            if (signal.dropNFlipBuySignal()) {
                return signal.dropNFlipStrength();
            } else if (signal.popNFlipSellSignal()) {
                return signal.popNFlipStrength();
            }
        }
        
        return 0.0; // 시그널이 없는 경우
    }
}
