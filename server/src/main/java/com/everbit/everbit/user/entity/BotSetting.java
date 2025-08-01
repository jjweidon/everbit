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
    private Strategy strategy = Strategy.BOLLINGER_MEAN_REVERSION;

    // 거래할 마켓 목록 (예: KRW-BTC, KRW-ETH 등)
    @Builder.Default
    @Column(name = "market")
    @Enumerated(EnumType.STRING)
    @ElementCollection
    @CollectionTable(name = "bot_market_list", joinColumns = @JoinColumn(name = "bot_setting_id"))
    private List<Market> marketList = new ArrayList<>(List.of(Market.BTC));

    // 최소 주문 금액
    @Builder.Default
    @Column(nullable = false)
    private Long baseOrderAmount = 6000L; // 최소 주문 금액 (KRW)

    // 최대 주문 금액
    @Builder.Default
    @Column(nullable = false)
    private Long maxOrderAmount = 10000L; // 최대 주문 금액 (KRW)

    public static BotSetting init(User user) {
        return BotSetting.builder()
                .user(user)
                .build();
    }

    public void update(BotSettingRequest request) {
        this.strategy = request.strategy();
        this.marketList = request.marketList();
        this.baseOrderAmount = request.baseOrderAmount();
        this.maxOrderAmount = request.maxOrderAmount();
    }
}
