package com.everbit.everbit.integrations.kakao;

import com.everbit.everbit.auth.config.AuthProperties;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class WebClientKakaoTokenClient implements KakaoTokenClient {

	private final AuthProperties authProperties;
	private final WebClient.Builder webClientBuilder;

	@Override
	public KakaoTokenResponse exchangeToken(String code) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "authorization_code");
		form.add("client_id", authProperties.kakaoClientId());
		form.add("redirect_uri", authProperties.kakaoRedirectUri());
		form.add("code", code);
		form.add("client_secret", authProperties.kakaoClientSecret());

		return webClientBuilder.build()
			.post()
			.uri(Objects.requireNonNull(authProperties.kakaoTokenUri()))
			.contentType(Objects.requireNonNull(MediaType.APPLICATION_FORM_URLENCODED))
			.bodyValue(form)
			.retrieve()
			.bodyToMono(KakaoTokenResponse.class)
			.block();
	}

	@Override
	public KakaoUserResponse getUserInfo(String accessToken) {
		return webClientBuilder.build()
			.get()
			.uri(Objects.requireNonNull(authProperties.kakaoUserInfoUri()))
			.header("Authorization", "Bearer " + accessToken)
			.retrieve()
			.bodyToMono(KakaoUserResponse.class)
			.block();
	}
}
