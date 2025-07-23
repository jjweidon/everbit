package com.everbit.everbit.user.entity;

import com.everbit.everbit.global.entity.BaseTime;
import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.trade.entity.enums.SignalType;

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
    
    @Enumerated(EnumType.STRING)
    private SignalType signalType;

    // 거래할 마켓 목록 (예: KRW-BTC, KRW-ETH 등)
    @ElementCollection
    @CollectionTable(name = "bot_market_list", joinColumns = @JoinColumn(name = "bot_setting_id"))
    @Column(name = "market")
    private List<Market> marketList;

    // 매수 설정
    @Column(nullable = false)
    private Double buyRatio; // 매수 비율 (0.0 ~ 1.0)

    @Column(nullable = false)
    private Long minBuyAmount; // 최소 주문 금액 (KRW)

    @Column(nullable = false)
    private Long maxBuyAmount; // 최대 주문 금액 (KRW)

    // 매도 설정
    @Column(nullable = false)
    private Double sellRatio; // 매도 비율 (0.0 ~ 1.0)

    @Column(nullable = false)
    private Double minSellVolume; // 최소 주문 수량

    @Column(nullable = false)
    private Double maxSellVolume; // 최대 주문 수량

    // 봇 실행 기간
    @Column(nullable = false)
    private LocalDateTime startTime; // 봇 시작 시간

    @Column
    private LocalDateTime endTime; // 봇 종료 시간 (null인 경우 무기한)

    // 봇 활성화 상태
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = false;
}
