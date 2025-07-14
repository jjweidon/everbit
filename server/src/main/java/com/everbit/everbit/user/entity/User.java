package com.everbit.everbit.user.entity;

import com.everbit.everbit.global.entity.BaseTime;
import com.everbit.everbit.user.entity.enums.Role;

import jakarta.persistence.*;
import lombok.*;
import de.huxhorn.sulky.ulid.ULID;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTime {
    @Id
    @Column(name = "user_id")
    @Builder.Default
    private final String id = new ULID().nextULID();

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String nickname;

    private String image;

    @Setter
    private boolean isUpbitConnected;
}