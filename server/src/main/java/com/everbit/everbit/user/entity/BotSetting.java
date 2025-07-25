package com.everbit.everbit.user.entity;

import com.everbit.everbit.global.entity.BaseTime;
import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.trade.entity.enums.Strategy;
import com.everbit.everbit.trade.entity.enums.CandleInterval;

import jakarta.persistence.*;
import lombok.*;
import de.huxhorn.sulky.ulid.ULID;

import java.time.LocalDateTime;
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
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Strategy strategy = Strategy.MOMENTUM;

    // 거래할 마켓 목록 (예: KRW-BTC, KRW-ETH 등)
    @ElementCollection
    @CollectionTable(name = "bot_market_list", joinColumns = @JoinColumn(name = "bot_setting_id"))
    @Column(name = "market")
    private List<Market> marketList;

    // 매수 설정
    @Builder.Default
    @Column(nullable = false)
    private Double buyRatio = 0.25; // 매수 비율 (0.0 ~ 1.0)

    @Builder.Default
    @Column(nullable = false)
    private Long minBuyAmount = 5000L; // 최소 주문 금액 (KRW)

    @Builder.Default
    @Column(nullable = false)
    private Long maxBuyAmount = 1000000L; // 최대 주문 금액 (KRW)

    // 매도 설정
    @Builder.Default
    @Column(nullable = false)
    private Double sellRatio = 0.5; // 매도 비율 (0.0 ~ 1.0)

    @Builder.Default
    @Column(nullable = false)
    private Double minSellRatio = 0.0; // 최소 매도 비율

    @Builder.Default
    @Column(nullable = false)
    private Double maxSellRatio = 1.0; // 최대 매도 비율

    // 봇 실행 기간
    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime startTime = LocalDateTime.now(); // 봇 시작 시간

    @Column
    private LocalDateTime endTime; // 봇 종료 시간 (null인 경우 무기한)

    // 캔들 설정
    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CandleInterval candleInterval = CandleInterval.TEN; // 분봉 단위 (1, 3, 5, 10, 15, 30, 60, 240)

    @Builder.Default
    @Column(nullable = false)
    private Integer candleCoun = 10; // 분석할 캔들 개수

    // 봇 활성화 상태
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = false;

    public static BotSetting init(User user) {
        return BotSetting.builder()
            .user(user)
            .build();
    }
}
