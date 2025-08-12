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
        switch (strategy) {
            case STANDARD:
                return signal.dropNFlipBuySignal();
            case TRIPLE_INDICATOR_CONSERVATIVE:
                return signal.isTripleIndicatorConservativeBuySignal();
            case TRIPLE_INDICATOR_MODERATE:
                return signal.isTripleIndicatorModerateBuySignal();
            case TRIPLE_INDICATOR_AGGRESSIVE:
                return signal.isTripleIndicatorAggressiveBuySignal();
            case BB_RSI_COMBO:
                return signal.isBbRsiComboBuySignal();
            case RSI_MACD_COMBO:
                return signal.isRsiMacdComboBuySignal();
            case BB_MACD_COMBO:
                return signal.isBbMacdComboBuySignal();
            default:
                return signal.isTripleIndicatorModerateBuySignal();
        }
    }

    /**
     * 전략에 따라 매도 시그널 여부를 판단합니다.
     */
    public boolean determineSellSignal(TradingSignal signal, Strategy strategy) {
        switch (strategy) {
            case STANDARD:
                return signal.popNFlipSellSignal();
            case TRIPLE_INDICATOR_CONSERVATIVE:
                return signal.isTripleIndicatorConservativeSellSignal();
            case TRIPLE_INDICATOR_MODERATE:
                return signal.isTripleIndicatorModerateSellSignal();
            case TRIPLE_INDICATOR_AGGRESSIVE:
                return signal.isTripleIndicatorAggressiveSellSignal();
            case BB_RSI_COMBO:
                return signal.isBbRsiComboSellSignal();
            case RSI_MACD_COMBO:
                return signal.isRsiMacdComboSellSignal();
            case BB_MACD_COMBO:
                return signal.isBbMacdComboSellSignal();
            default:
                return signal.isTripleIndicatorModerateSellSignal();
        }
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
        switch (strategy) {
            case STANDARD:
                return calculateStandardSignalStrength(signal);
            case TRIPLE_INDICATOR_CONSERVATIVE:
                return calculateTripleIndicatorConservativeSignalStrength(signal);
            case TRIPLE_INDICATOR_MODERATE:
                return calculateTripleIndicatorModerateSignalStrength(signal);
            case TRIPLE_INDICATOR_AGGRESSIVE:
                return calculateTripleIndicatorAggressiveSignalStrength(signal);
            case BB_RSI_COMBO:
                return calculateBbRsiComboSignalStrength(signal);
            case RSI_MACD_COMBO:
                return calculateRsiMacdComboSignalStrength(signal);
            case BB_MACD_COMBO:
                return calculateBbMacdComboSignalStrength(signal);
            default:
                return 0.5; // 기본값
        }
    }

    /**
     * STANDARD 전략 (DROP_N_FLIP / POP_N_FLIP) 시그널 강도 계산
     */
    private double calculateStandardSignalStrength(TradingSignal signal) {
        // 매수 시그널인지 매도 시그널인지 확인
        if (signal.dropNFlipBuySignal()) {
            return signal.dropNFlipStrength();
        } else if (signal.popNFlipSellSignal()) {
            return signal.popNFlipStrength();
        }
        
        return 0.0; // 시그널이 없는 경우
    }

    /**
     * 3지표 보수전략 시그널 강도 계산
     */
    private double calculateTripleIndicatorConservativeSignalStrength(TradingSignal signal) {
        if (signal.isTripleIndicatorConservativeBuySignal()) {
            return calculateBuySignalStrength(signal, true);
        } else if (signal.isTripleIndicatorConservativeSellSignal()) {
            return calculateSellSignalStrength(signal, true);
        }
        return 0.0;
    }

    /**
     * 3지표 중간전략 시그널 강도 계산
     */
    private double calculateTripleIndicatorModerateSignalStrength(TradingSignal signal) {
        if (signal.isTripleIndicatorModerateBuySignal()) {
            return calculateBuySignalStrength(signal, false);
        } else if (signal.isTripleIndicatorModerateSellSignal()) {
            return calculateSellSignalStrength(signal, false);
        }
        return 0.0;
    }

    /**
     * 3지표 공격전략 시그널 강도 계산
     */
    private double calculateTripleIndicatorAggressiveSignalStrength(TradingSignal signal) {
        if (signal.isTripleIndicatorAggressiveBuySignal()) {
            return calculateBuySignalStrength(signal, false);
        } else if (signal.isTripleIndicatorAggressiveSellSignal()) {
            return calculateSellSignalStrength(signal, false);
        }
        return 0.0;
    }

    /**
     * 볼린저+RSI 조합 시그널 강도 계산
     */
    private double calculateBbRsiComboSignalStrength(TradingSignal signal) {
        if (signal.isBbRsiComboBuySignal()) {
            return calculateDetailedSignalStrength(signal, true);
        } else if (signal.isBbRsiComboSellSignal()) {
            return calculateDetailedSignalStrength(signal, false);
        }
        return 0.0;
    }

    /**
     * RSI+MACD 조합 시그널 강도 계산
     */
    private double calculateRsiMacdComboSignalStrength(TradingSignal signal) {
        if (signal.isRsiMacdComboBuySignal()) {
            return calculateDetailedSignalStrength(signal, true);
        } else if (signal.isRsiMacdComboSellSignal()) {
            return calculateDetailedSignalStrength(signal, false);
        }
        return 0.0;
    }

    /**
     * 볼린저+MACD 조합 시그널 강도 계산
     */
    private double calculateBbMacdComboSignalStrength(TradingSignal signal) {
        if (signal.isBbMacdComboBuySignal()) {
            return calculateDetailedSignalStrength(signal, true);
        } else if (signal.isBbMacdComboSellSignal()) {
            return calculateDetailedSignalStrength(signal, false);
        }
        return 0.0;
    }

    /**
     * 매수 시그널 강도 계산
     */
    private double calculateBuySignalStrength(TradingSignal signal, boolean isConservative) {
        int signalCount = signal.getBuySignalCount();
        
        if (isConservative) {
            // 보수전략: 모든 지표가 시그널을 보낼 때만 최대 강도
            return signalCount == 3 ? 1.0 : 0.0;
        } else {
            // 중간/공격전략: 시그널 개수에 따른 강도
            double baseStrength = signalCount / 3.0;
            double detailedStrength = calculateDetailedSignalStrength(signal, true);
            return (baseStrength + detailedStrength) / 2.0;
        }
    }

    /**
     * 매도 시그널 강도 계산
     */
    private double calculateSellSignalStrength(TradingSignal signal, boolean isConservative) {
        int signalCount = signal.getSellSignalCount();
        
        if (isConservative) {
            // 보수전략: 모든 지표가 시그널을 보낼 때만 최대 강도
            return signalCount == 3 ? 1.0 : 0.0;
        } else {
            // 중간/공격전략: 시그널 개수에 따른 강도
            double baseStrength = signalCount / 3.0;
            double detailedStrength = calculateDetailedSignalStrength(signal, false);
            return (baseStrength + detailedStrength) / 2.0;
        }
    }

    /**
     * 지표 값들의 차이에 따른 세부 시그널 강도 계산 (0.0 ~ 1.0)
     */
    private double calculateDetailedSignalStrength(TradingSignal signal, boolean isBuySignal) {
        double bbStrength = calculateBollingerBandsStrength(signal, isBuySignal);
        double rsiStrength = calculateRSIStrength(signal, isBuySignal);
        double macdStrength = calculateMACDStrength(signal, isBuySignal);
        
        // 활성화된 지표들의 강도 평균 계산
        int activeIndicators = 0;
        double totalStrength = 0.0;
        
        if (isBuySignal) {
            if (signal.bbBuySignal()) {
                totalStrength += bbStrength;
                activeIndicators++;
            }
            if (signal.rsiBuySignal()) {
                totalStrength += rsiStrength;
                activeIndicators++;
            }
            if (signal.macdBuySignal()) {
                totalStrength += macdStrength;
                activeIndicators++;
            }
        } else {
            if (signal.bbSellSignal()) {
                totalStrength += bbStrength;
                activeIndicators++;
            }
            if (signal.rsiSellSignal()) {
                totalStrength += rsiStrength;
                activeIndicators++;
            }
            if (signal.macdSellSignal()) {
                totalStrength += macdStrength;
                activeIndicators++;
            }
        }
        
        return activeIndicators > 0 ? totalStrength / activeIndicators : 0.0;
    }

    /**
     * 볼린저밴드 강도 계산
     */
    private double calculateBollingerBandsStrength(TradingSignal signal, boolean isBuySignal) {
        double currentPrice = signal.currentPrice().doubleValue();
        double bbLower = signal.bbLowerBand().doubleValue();
        double bbMiddle = signal.bbMiddleBand().doubleValue();
        double bbUpper = signal.bbUpperBand().doubleValue();
        
        if (isBuySignal) {
            // 매수: 현재가가 하단 밴드 아래에 있을 때
            if (currentPrice < bbLower) {
                // 하단 밴드와의 거리를 계산 (0.0 ~ 1.0)
                double bandWidth = bbMiddle - bbLower;
                double distance = bbLower - currentPrice;
                return Math.min(distance / bandWidth, 1.0);
            }
        } else {
            // 매도: 현재가가 상단 밴드 위에 있을 때
            if (currentPrice > bbUpper) {
                // 상단 밴드와의 거리를 계산 (0.0 ~ 1.0)
                double bandWidth = bbUpper - bbMiddle;
                double distance = currentPrice - bbUpper;
                return Math.min(distance / bandWidth, 1.0);
            }
        }
        
        return 0.0;
    }

    /**
     * RSI 강도 계산
     */
    private double calculateRSIStrength(TradingSignal signal, boolean isBuySignal) {
        double rsiValue = signal.rsiValue().doubleValue();
        
        if (isBuySignal) {
            // 매수: RSI가 30 이하일 때
            if (rsiValue < 30) {
                // RSI가 0에 가까울수록 강한 시그널 (0.0 ~ 1.0)
                return (30 - rsiValue) / 30;
            }
        } else {
            // 매도: RSI가 70 이상일 때
            if (rsiValue > 70) {
                // RSI가 100에 가까울수록 강한 시그널 (0.0 ~ 1.0)
                return (rsiValue - 70) / (100 - 70);
            }
        }
        
        return 0.0;
    }

    /**
     * MACD 히스토그램 강도 계산
     */
    private double calculateMACDStrength(TradingSignal signal, boolean isBuySignal) {
        double macdValue = signal.macdValue().doubleValue();
        double histogram = signal.macdHistogram().doubleValue();
        
        // MACD 히스토그램의 절대값을 기준으로 강도 계산
        double absHistogram = Math.abs(histogram);
        
        // 히스토그램이 0에 가까우면 약한 시그널, 멀수록 강한 시그널
        // 일반적으로 MACD 히스토그램의 범위를 0.1% ~ 1%로 가정
        double maxExpectedHistogram = Math.abs(macdValue) * 0.01; // MACD 값의 1%를 최대값으로 가정
        
        if (maxExpectedHistogram > 0) {
            return Math.min(absHistogram / maxExpectedHistogram, 1.0);
        }
        
        return 0.5; // 기본값
    }
}
