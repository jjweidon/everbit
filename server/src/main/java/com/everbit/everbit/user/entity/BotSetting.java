package com.everbit.everbit.user.entity;

import com.everbit.everbit.global.entity.BaseTime;
import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.trade.entity.enums.Strategy;
import com.everbit.everbit.user.dto.BotSettingRequest;

import jakarta.persistence.*;
import lombok.*;
import de.huxhorn.sulky.ulid.ULID;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BotSetting extends BaseTime {
    @Id
    @Column(name = "bot_setting_id")
    @Builder.Default
    private final String id = new ULID().nextULID();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 거래할 마켓 목록 (예: KRW-BTC, KRW-ETH 등)
    @Builder.Default
    @Column(name = "market")
    @Enumerated(EnumType.STRING)
    @ElementCollection
    @CollectionTable(name = "bot_market_list", joinColumns = @JoinColumn(name = "bot_setting_id"))
    private List<Market> marketList = new ArrayList<>(List.of(Market.BTC));

    // 매수 활성화 여부
    @Builder.Default
    private Boolean isBuyActive = true;

    // 매도 활성화 여부
    @Builder.Default
    private Boolean isSellActive = true;

    // 매수 전략
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "buy_strategy")
    private Strategy buyStrategy = Strategy.EXTREME_FLIP;

    // 매도 전략
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "sell_strategy")
    private Strategy sellStrategy = Strategy.EXTREME_FLIP;

    // 매수 최소 주문 금액
    @Builder.Default
    @Column(nullable = false)
    private Long buyBaseOrderAmount = 6000L; // 매수 최소 주문 금액 (KRW)

    // 매수 최대 주문 금액
    @Builder.Default
    @Column(nullable = false)
    private Long buyMaxOrderAmount = 10000L; // 매수 최대 주문 금액 (KRW)

    // 매도 최소 주문 금액
    @Builder.Default
    @Column(nullable = false)
    private Long sellBaseOrderAmount = 6000L; // 매도 최소 주문 금액 (KRW)

    // 매도 최대 주문 금액
    @Builder.Default
    @Column(nullable = false)
    private Long sellMaxOrderAmount = 10000L; // 매도 최대 주문 금액 (KRW)

    // 손실 임계값
    @Builder.Default
    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal lossThreshold = new BigDecimal("0.01"); // 1% 손실 임계값

    // 이익 임계값
    @Builder.Default
    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal profitThreshold = new BigDecimal("0.02"); // 2% 이익 임계값

    // 손실 매도 비율
    @Builder.Default
    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal lossSellRatio = new BigDecimal("0.9"); // 손실 매도 비율

    // 이익 매도 비율
    @Builder.Default
    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal profitSellRatio = new BigDecimal("0.5"); // 이익 매도 비율

    // 손실 관리 활성화 여부
    @Builder.Default
    private Boolean isLossManagementActive = true;

    // 이익 관리 활성화 여부
    @Builder.Default
    private Boolean isProfitTakingActive = true;

    // 시간초과 관리 활성화 여부
    @Builder.Default
    private Boolean isTimeOutSellActive = true;

    // 시간초과 관리 시간
    @Builder.Default
    private int timeOutSellMinutes = 45;

    // 시간초과 관리 이익 비율
    @Builder.Default
    @Column(precision = 4, scale = 3)
    private BigDecimal timeOutSellProfitRatio = new BigDecimal("0.001");

    public static BotSetting init(User user) {
        return BotSetting.builder()
                .user(user)
                .build();
    }

    public void update(BotSettingRequest request) {
        this.marketList = request.marketList();
        this.isBuyActive = request.isBuyActive();
        this.isSellActive = request.isSellActive();
        this.buyStrategy = request.buyStrategy();
        this.sellStrategy = request.sellStrategy();
        this.buyBaseOrderAmount = request.buyBaseOrderAmount();
        this.buyMaxOrderAmount = request.buyMaxOrderAmount();
        this.sellBaseOrderAmount = request.sellBaseOrderAmount();
        this.sellMaxOrderAmount = request.sellMaxOrderAmount();
        this.lossThreshold = request.lossThreshold();
        this.profitThreshold = request.profitThreshold();
        this.lossSellRatio = request.lossSellRatio();
        this.profitSellRatio = request.profitSellRatio();
        this.isLossManagementActive = request.isLossManagementActive();
        this.isProfitTakingActive = request.isProfitTakingActive();
        this.isTimeOutSellActive = request.isTimeOutSellActive();
        this.timeOutSellMinutes = request.timeOutSellMinutes();
        this.timeOutSellProfitRatio = request.timeOutSellProfitRatio();
    }
}
