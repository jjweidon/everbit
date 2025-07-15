package com.everbit.everbit.oauth2.service;

import com.everbit.everbit.global.jwt.TokenDto;
import com.everbit.everbit.oauth2.dto.CustomOAuth2User;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("OAuth2 사용자 정보 로딩 시작");
        log.info("Client Registration ID: {}", userRequest.getClientRegistration().getRegistrationId());
        log.info("Access Token: {}", userRequest.getAccessToken().getTokenValue());
        
        try {
            OAuth2User oAuth2User = super.loadUser(userRequest);
            log.info("OAuth2 사용자 정보 로드 성공");
            
            User user = userService.createUser(oAuth2User);
            log.info("사용자 생성/조회 완료 - ID: {}, 이메일: {}", user.getId(), user.getUsername());

            TokenDto tokenDto = TokenDto.create(user.getUsername(), user.getRole().name());
            log.info("토큰 DTO 생성 완료");

            return new CustomOAuth2User(tokenDto);
        } catch (Exception e) {
            log.error("OAuth2 사용자 정보 로딩 중 오류 발생", e);
            throw e;
        }
    }
}