package com.everbit.everbit.oauth2.dto;

import com.everbit.everbit.global.dto.Response;

public record KakaoLoginResponse(String redirectUrl) implements Response {
} 