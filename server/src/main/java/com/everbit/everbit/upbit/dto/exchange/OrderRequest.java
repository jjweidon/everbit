package com.everbit.everbit.upbit.dto.exchange;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OrderRequest(
    @NotNull(message = "market is required")
    String market,
    
    @NotNull(message = "side is required")
    String side,
    
    @NotNull(message = "side is required")
    String volume,
    
    @NotNull(message = "side is required")
    String price,
    
    @NotNull(message = "ord_type is required")
    String ordType,
    
    String identifier,
    
    String timeInForce,
    
    String smpType
) {
    public static class Side {
        public static final String BID = "bid";  // 매수
        public static final String ASK = "ask";  // 매도
    }

    public static class OrdType {
        public static final String LIMIT = "limit";    // 지정가 주문
        public static final String PRICE = "price";    // 시장가 주문(매수)
        public static final String MARKET = "market";  // 시장가 주문(매도)
        public static final String BEST = "best";      // 최유리 주문
    }

    public static class TimeInForce {
        public static final String IOC = "ioc";           // Immediate or Cancel
        public static final String FOK = "fok";           // Fill or Kill
        public static final String POST_ONLY = "post_only"; // Post Only
    }

    public static class SmpType {
        public static final String REDUCE = "reduce";         // 수량 감소
        public static final String CANCEL_MAKER = "cancel_maker"; // maker 주문 취소
        public static final String CANCEL_TAKER = "cancel_taker"; // 현재 주문 취소
    }
} 