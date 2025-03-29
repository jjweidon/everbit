package com.everbit.everbit.dto.upbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Upbit 캔들 정보 DTO
 */
@Data
public class CandleDto {
    
    /**
     * 마켓 코드
     */
    private String market;
    
    /**
     * 캔들 기준 시각 (UTC)
     */
    @JsonProperty("candle_date_time_utc")
    private String candleDateTimeUtc;
    
    /**
     * 캔들 기준 시각 (KST)
     */
    @JsonProperty("candle_date_time_kst")
    private String candleDateTimeKst;
    
    /**
     * 시가
     */
    @JsonProperty("opening_price")
    private BigDecimal openingPrice;
    
    /**
     * 고가
     */
    @JsonProperty("high_price")
    private BigDecimal highPrice;
    
    /**
     * 저가
     */
    @JsonProperty("low_price")
    private BigDecimal lowPrice;
    
    /**
     * 종가
     */
    @JsonProperty("trade_price")
    private BigDecimal tradePrice;
    
    /**
     * 해당 캔들의 타임스탬프
     */
    private Long timestamp;
    
    /**
     * 누적 거래 금액
     */
    @JsonProperty("candle_acc_trade_price")
    private BigDecimal candleAccTradePrice;
    
    /**
     * 누적 거래량
     */
    @JsonProperty("candle_acc_trade_volume")
    private BigDecimal candleAccTradeVolume;
    
    /**
     * 분 단위
     */
    private Integer unit;
    
    /**
     * 전환된 로컬 시간
     */
    public LocalDateTime getDateTime() {
        return LocalDateTime.parse(candleDateTimeUtc.replace("T", " ").replace("Z", ""));
    }
} 