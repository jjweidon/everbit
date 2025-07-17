package com.everbit.everbit.support.entity;

import com.everbit.everbit.global.entity.BaseTime;
import com.everbit.everbit.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import de.huxhorn.sulky.ulid.ULID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry extends BaseTime {
    @Id
    @Column(name = "inquiry_id")
    @Builder.Default
    private final String id = new ULID().nextULID();

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public static Inquiry of(String content, User user) {
        return Inquiry.builder()
            .content(content)
            .user(user)
            .build();
    }
} 