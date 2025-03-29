package com.everbit.everbit.dto.upbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Upbit 시세 정보 DTO
 */
@Data
public class TickerDto {
    
    /**
     * 마켓 코드 (예: KRW-BTC)
     */
    private String market;
    
    /**
     * 거래 대금
     */
    @JsonProperty("acc_trade_price_24h")
    private BigDecimal accTradePrice24h;
    
    /**
     * 24시간 거래량
     */
    @JsonProperty("acc_trade_volume_24h")
    private BigDecimal accTradeVolume24h;
    
    /**
     * 현재가
     */
    @JsonProperty("trade_price")
    private BigDecimal tradePrice;
    
    /**
     * 최고가
     */
    @JsonProperty("high_price")
    private BigDecimal highPrice;
    
    /**
     * 최저가
     */
    @JsonProperty("low_price")
    private BigDecimal lowPrice;
    
    /**
     * 시가
     */
    @JsonProperty("opening_price")
    private BigDecimal openingPrice;
    
    /**
     * 전일 종가
     */
    @JsonProperty("prev_closing_price")
    private BigDecimal prevClosingPrice;
    
    /**
     * 변화율
     */
    @JsonProperty("signed_change_rate")
    private BigDecimal signedChangeRate;
    
    /**
     * 부호가 있는 변화량
     */
    @JsonProperty("signed_change_price")
    private BigDecimal signedChangePrice;
    
    /**
     * 타임스탬프
     */
    private Long timestamp;
} 