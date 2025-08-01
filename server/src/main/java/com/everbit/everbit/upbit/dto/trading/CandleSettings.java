package com.everbit.everbit.upbit.dto.trading;

import com.everbit.everbit.trade.entity.enums.Strategy;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 전략별 최적화된 캔들 설정을 관리하는 클래스
 * 
 * 각 전략의 특성에 맞춰 최적의 시간프레임과 데이터 양을 설정합니다:
 * 
 * 1. BOLLINGER_MEAN_REVERSION (15분, 100개)
 *    - 평균회귀 전략은 중기적 가격 변동을 포착해야 함
 *    - 볼린저 밴드(20기간) + RSI(14기간) 계산을 위해 충분한 데이터 필요
 *    - 약 25시간의 데이터로 안정적인 신호 생성
 * 
 * 2. BB_MOMENTUM (10분, 120개)
 *    - 모멘텀 전략은 빠른 진입/청산이 중요
 *    - 볼린저 밴드 이탈을 정확히 포착하기 위해 충분한 데이터 필요
 *    - 약 20시간의 데이터로 모멘텀 변화 감지
 * 
 * 3. EMA_MOMENTUM (5분, 150개)
 *    - EMA 교차는 빠른 반응이 필요
 *    - MACD 신호와 함께 단기 추세 변화를 포착
 *    - 약 12.5시간의 데이터로 단기 추세 분석
 * 
 * 4. ENSEMBLE (10분, 120개)
 *    - 여러 전략을 조합하므로 균형잡힌 시간프레임
 *    - 개별 전략들의 신호를 종합적으로 분석
 * 
 * 5. ENHANCED_ENSEMBLE (15분, 100개)
 *    - 가장 정교한 전략이므로 안정적인 시간프레임
 *    - 충분한 데이터로 신뢰도 높은 신호 생성
 */
@Getter
@AllArgsConstructor
public class CandleSettings {
    private final int interval; // 분 단위
    private final int count;    // 캔들 개수
    
    public static CandleSettings getOptimalSettings(Strategy strategy) {
        switch (strategy) {
            case BOLLINGER_MEAN_REVERSION:
                return new CandleSettings(15, 100); // 15분, 100개 (약 25시간)
            case BB_MOMENTUM:
                return new CandleSettings(10, 120); // 10분, 120개 (약 20시간)
            case EMA_MOMENTUM:
                return new CandleSettings(5, 150);  // 5분, 150개 (약 12.5시간)
            case ENSEMBLE:
                return new CandleSettings(10, 120); // 10분, 120개 (약 20시간)
            case ENHANCED_ENSEMBLE:
                return new CandleSettings(15, 100); // 15분, 100개 (약 25시간)
            default:
                return new CandleSettings(10, 100); // 기본값
        }
    }
} 