package com.everbit.everbit.trade.entity;

import com.everbit.everbit.global.entity.BaseTime;
import com.everbit.everbit.trade.entity.enums.TradeStatus;
import com.everbit.everbit.trade.entity.enums.TradeType;
import com.everbit.everbit.trade.entity.enums.SignalType;
import com.everbit.everbit.user.entity.User;
import de.huxhorn.sulky.ulid.ULID;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trade extends BaseTime {
    @Id
    @Column(name = "trade_id")
    @Builder.Default
    private final String id = new ULID().nextULID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String market; // 거래 마켓 (예: KRW-BTC)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeType type; // 매수/매도 구분

    @Column(nullable = false)
    private String orderId; // 업비트 주문 ID

    @Column(nullable = false)
    private BigDecimal price; // 주문 가격

    @Column(nullable = false)
    private BigDecimal amount; // 주문 수량

    @Column(nullable = false)
    private BigDecimal totalPrice; // 총 주문 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status; // 주문 상태

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SignalType signalType; // 매매 시그널 정보 (예: MACD_CROSS, RSI_OVERSOLD 등)
}
