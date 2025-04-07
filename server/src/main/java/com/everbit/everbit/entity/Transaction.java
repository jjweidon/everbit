package com.everbit.everbit.entity;

import com.everbit.everbit.entity.enums.TransactionType;
import com.everbit.everbit.entity.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import de.huxhorn.sulky.ulid.ULID;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction extends BaseTime {
    @Id
    @Column(name = "transaction_id")
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
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;
    
    @Column(nullable = false, precision = 20, scale = 0)
    private BigDecimal price;
    
    @Column(precision = 20, scale = 8)
    private BigDecimal fee;
    
    @Column(nullable = false, precision = 20, scale = 0)
    private BigDecimal total;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;
    
    private LocalDateTime executedAt;
    
    @Column(length = 36)
    private String upbitOrderId;
} 