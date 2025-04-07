package com.everbit.everbit.entity;

import com.everbit.everbit.entity.enums.StrategyType;
import de.huxhorn.sulky.ulid.ULID;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradingStrategy extends BaseTime {
    @Id
    @Column(name = "strategy_id")
    @Builder.Default
    private final String id = new ULID().nextULID();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upbit_account_id")
    private UpbitAccount upbitAccount;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StrategyType strategyType;
    
    @Column(nullable = false)
    private String symbol;
    
    @Column(precision = 20, scale = 0)
    private BigDecimal budget;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = false;
    
    @Column(columnDefinition = "TEXT")
    private String parameters;
} 