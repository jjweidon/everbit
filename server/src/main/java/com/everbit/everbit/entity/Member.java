package com.everbit.everbit.entity;

import com.everbit.everbit.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import de.huxhorn.sulky.ulid.ULID;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTime {
    @Id
    @Column(name = "member_id")
    @Builder.Default
    private final String id = new ULID().nextULID();

    @Column(nullable = false)
    private String username;

    private String image;

    @Setter
    private String upbitAccessKey;

    @Setter
    private String upbitSecretKey;

    @Setter
    private boolean isUpbitConnected;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String nickname;

}