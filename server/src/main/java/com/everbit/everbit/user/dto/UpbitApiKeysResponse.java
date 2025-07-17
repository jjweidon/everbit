package com.everbit.everbit.user.dto;

import com.everbit.everbit.global.dto.Response;
import com.everbit.everbit.user.entity.User;

import lombok.Builder;

@Builder
public record UpbitApiKeysResponse(
    String accessKey,
    String secretKey
) implements Response {
    public static UpbitApiKeysResponse from(User user) {
        return UpbitApiKeysResponse.builder()
            .accessKey(user.getUpbitAccessKey())
            .secretKey(user.getUpbitSecretKey())
            .build();
    }
}
