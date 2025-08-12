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
public class CustomSignal extends BaseTime {
    @Id
    @Column(name = "custom_signal_id")
    @Builder.Default
    private final String id = new ULID().nextULID();

    @Enumerated(EnumType.STRING)
    private Market market;

    private LocalDateTime lastDropAt;

    private LocalDateTime lastPopAt;

    @Builder.Default
    private int consecutiveDropCount = 0;

    @Builder.Default
    private int consecutivePopCount = 0;

    private LocalDateTime lastFlipUpAt;

    private LocalDateTime lastFlipDownAt;

    public void countUpConsecutiveDrop() {
        this.lastDropAt = LocalDateTime.now();
        this.consecutiveDropCount += 1;
    }

    public void resetConsecutiveDrop() {
        this.lastDropAt = null;
        this.consecutiveDropCount = 0;
    }

    public void countUpConsecutivePop() {
        this.lastPopAt = LocalDateTime.now();
        this.consecutivePopCount += 1;
    }

    public void resetConsecutivePop() {
        this.lastPopAt = null;
        this.consecutivePopCount = 0;
    }

    public void updateLastFlipUpAt() {
        this.lastFlipUpAt = LocalDateTime.now();
    }

    public void updateLastFlipDownAt() {
        this.lastFlipDownAt = LocalDateTime.now();
    }
}
