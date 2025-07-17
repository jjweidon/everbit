package com.everbit.everbit.upbit.entity;

import com.everbit.everbit.global.entity.BaseTime;
import com.everbit.everbit.user.entity.User;

import de.huxhorn.sulky.ulid.ULID;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseTime {
    @Id
    @Column(name = "account_id")
    @Builder.Default
    private final String id = new ULID().nextULID();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Setter
    private String upbitAccessKey;

    @Setter
    private String upbitSecretKey;

    public static Account init(User user) {
        return Account.builder()
                .user(user)
                .build();
    }

    public void updateKeys(String accessKey, String secretKey) {
        this.upbitAccessKey = accessKey;
        this.upbitSecretKey = secretKey;
    }
}
