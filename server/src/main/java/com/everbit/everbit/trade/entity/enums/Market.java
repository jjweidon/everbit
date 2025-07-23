package com.everbit.everbit.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Market {
    BTC("KRW-BTC"),
    ETH("KRW-ETH"),
    STRIKE("KRW-STRIKE"),
    XRP("KRW-XRP"),
    DOGE("KRW-DOGE"),
    PENGU("KRW-PENGU"),
    SOL("KRW-SOL"),
    ARDR("KRW-ARDR"),
    STRAX("KRW-STRAX"),
    USDT("KRW-USDT"),
    ENS("KRW-ENS"),
    AERGO("KRW-AERGO");

    private final String code;

    public static Market fromCode(String code) {
        for (Market market : Market.values()) {
            if (market.getCode().equals(code)) {
                return market;
            }
        }
        throw new IllegalArgumentException("Unknown market code: " + code);
    }
}
