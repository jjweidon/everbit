package com.everbit.everbit.upbit.entity;

import java.time.LocalDateTime;

import com.everbit.everbit.global.entity.BaseTime;
import com.everbit.everbit.trade.entity.enums.Market;

import jakarta.persistence.*;
import lombok.*;
import de.huxhorn.sulky.ulid.ULID;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketSignal extends BaseTime {
    @Id
    @Column(name = "market_signal_id")
    @Builder.Default
    private final String id = new ULID().nextULID();

    @Enumerated(EnumType.STRING)
    private Market market;

    private LocalDateTime lastOversoldAt;

    @Builder.Default
    private int consecutiveOversoldCount = 0;

    public void countUpConsecutiveOversoldCount() {
        this.lastOversoldAt = LocalDateTime.now();
        this.consecutiveOversoldCount += 1;
    }

    public void resetConsecutiveOversoldCount() {
        this.lastOversoldAt = null;
        this.consecutiveOversoldCount = 0;
    }
}
