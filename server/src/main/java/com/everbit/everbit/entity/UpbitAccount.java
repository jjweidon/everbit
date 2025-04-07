package com.everbit.everbit.entity;

import de.huxhorn.sulky.ulid.ULID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import jakarta.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpbitAccount extends BaseTime {
    @Id
    @Column(name = "upbit_account_id")
    @Builder.Default
    private final String id = new ULID().nextULID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private String accountName;

    // 사용자 계정 정보
    @Column(nullable = false)
    private String upbitUsername;

    // 암호화된 계정 비밀번호 또는 토큰 정보
    @Column(nullable = false)
    private String encryptedCredential;

    @Builder.Default
    private boolean isActive = true;
}
