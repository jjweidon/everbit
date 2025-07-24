package com.everbit.everbit.user.entity;

import com.everbit.everbit.global.entity.BaseTime;
import com.everbit.everbit.oauth2.dto.OAuth2Response;
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

    @Column(nullable = false, unique = true)
    private String username;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String email;

    private String nickname;

    private String image;

    private String upbitAccessKey;

    private String upbitSecretKey;

    @Builder.Default
    private Boolean isBotActive = false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private BotSetting botSetting;

    public static User init(OAuth2Response oAuth2Response) {
        String username = oAuth2Response.getProvider() + "-" + oAuth2Response.getProviderId();
        return User.builder()
                .username(username)
                .role(Role.ROLE_USER)
                .nickname(oAuth2Response.getName())
                .image(oAuth2Response.getImage())
                .build();
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateKeys(String accessKey, String secretKey) {
        this.upbitAccessKey = accessKey;
        this.upbitSecretKey = secretKey;
    }

    public void toggleBotActive() {
        this.isBotActive = !this.isBotActive;
    }

    public void setBotSetting(BotSetting botSetting) {
        this.botSetting = botSetting;
    }
}