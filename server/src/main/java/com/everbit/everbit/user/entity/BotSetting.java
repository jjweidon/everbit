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
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "buy_strategy")
    private Strategy buyStrategy = Strategy.TRIPLE_INDICATOR_MODERATE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "sell_strategy")
    private Strategy sellStrategy = Strategy.TRIPLE_INDICATOR_MODERATE;

    // 거래할 마켓 목록 (예: KRW-BTC, KRW-ETH 등)
    @Builder.Default
    @Column(name = "market")
    @Enumerated(EnumType.STRING)
    @ElementCollection
    @CollectionTable(name = "bot_market_list", joinColumns = @JoinColumn(name = "bot_setting_id"))
    private List<Market> marketList = new ArrayList<>(List.of(Market.BTC));

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

    public static BotSetting init(User user) {
        return BotSetting.builder()
                .user(user)
                .build();
    }

    public void update(BotSettingRequest request) {
        this.buyStrategy = request.buyStrategy();
        this.sellStrategy = request.sellStrategy();
        this.marketList = request.marketList();
        this.buyBaseOrderAmount = request.buyBaseOrderAmount();
        this.buyMaxOrderAmount = request.buyMaxOrderAmount();
        this.sellBaseOrderAmount = request.sellBaseOrderAmount();
        this.sellMaxOrderAmount = request.sellMaxOrderAmount();
    }
}
