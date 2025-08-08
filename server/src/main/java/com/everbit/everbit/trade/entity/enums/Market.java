package com.everbit.everbit.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Market {
    BTC("KRW-BTC", "비트코인"),
    ETH("KRW-ETH", "이더리움"),
    SOL("KRW-SOL", "솔라나"),
    DOGE("KRW-DOGE", "도지코인"),
    USDT("KRW-USDT", "테더"),
    STRIKE("KRW-STRIKE", "스트라이크"),
    XRP("KRW-XRP", "리플"),
    PENGU("KRW-PENGU", "펏지펭귄"),
    ENA("KRW-ENA", "에테나"),
    ADA("KRW-ADA", "에이다");

    private final String code;
    private final String description;

    public static Market fromCode(String code) {
        for (Market market : Market.values()) {
            if (market.getCode().equals(code)) {
                return market;
            }
        }
        throw new IllegalArgumentException("Unknown market code: " + code);
    }
}
