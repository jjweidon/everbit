package com.everbit.everbit.user.dto;

import com.everbit.everbit.global.dto.Response;
import com.everbit.everbit.user.entity.User;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UserResponse(
    String userId,
    String username,
    String email,
    String nickname,
    String image,
    String createdAt,
    Boolean isUpbitConnected,
    String role,
    Boolean isBotActive
) implements Response {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getNickname(),
            user.getImage(),
            user.getCreatedAt().toString(),
            user.getUpbitAccessKey() != null && user.getUpbitSecretKey() != null,
            user.getRole().name(),
            user.getIsBotActive()
        );
    }
}