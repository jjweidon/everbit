package com.everbit.everbit.user.dto;

import com.everbit.everbit.global.dto.Response;

import lombok.Builder;

@Builder
public record UpbitApiKeysResponse(
    String accessKey,
    String secretKey
) implements Response {
    public static UpbitApiKeysResponse of(String accessKey, String secretKey) {
        return UpbitApiKeysResponse.builder()
            .accessKey(accessKey)
            .secretKey(secretKey)
            .build();
    }
}
