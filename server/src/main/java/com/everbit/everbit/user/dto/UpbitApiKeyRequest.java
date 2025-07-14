package com.everbit.everbit.user.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UpbitApiKeyRequest(
    String accessKey,
    String secretKey
) {}