package com.everbit.everbit.global.jwt;

import lombok.Builder;

@Builder
public record TokenDto(
    String username,
    String role
) {
    public static TokenDto create(String username, String role) {
        return TokenDto.builder()
                .username(username)
                .role(role)
                .build();
    }
}
