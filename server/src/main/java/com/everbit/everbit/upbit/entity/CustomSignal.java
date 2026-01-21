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

    private Double previousAtr;  // 이전 ATR 값 (변동성 감소 확인용)

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

    public void setConsecutiveDropCountMin(int minConsecutiveCount) {
        this.consecutiveDropCount = minConsecutiveCount;
    }

    public void setConsecutivePopCountMin(int minConsecutiveCount) {
        this.consecutivePopCount = minConsecutiveCount;
    }

    /**
     * ATR 값을 업데이트합니다.
     */
    public void updateAtr(double currentATR) {
        this.previousAtr = currentATR;
    }

}
