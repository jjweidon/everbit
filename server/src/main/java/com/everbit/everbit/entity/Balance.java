package com.everbit.everbit.entity;

import jakarta.persistence.*;
import lombok.*;
import de.huxhorn.sulky.ulid.ULID;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Balance extends BaseTime {
    @Id
    @Column(name = "balance_id")
    @Builder.Default
    private final String id = new ULID().nextULID();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upbit_account_id")
    private UpbitAccount upbitAccount;
    
    @Column(nullable = false)
    private String symbol;
    
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;
    
    @Column(precision = 20, scale = 0)
    private BigDecimal averagePrice;
    
    @Column(precision = 20, scale = 0)
    private BigDecimal currentPrice;
    
    @Column(precision = 20, scale = 0)
    private BigDecimal totalValue;
} 