package com.everbit.everbit.oauth2.dto;

import com.everbit.everbit.global.dto.Response;

public record TokenResponse(String accessToken, String refreshToken) implements Response {
}

