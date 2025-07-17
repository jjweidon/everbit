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
        OAuth2User oAuth2User = super.loadUser(userRequest);
        User user = userService.createOrFindUser(oAuth2User);
        TokenDto tokenDto = TokenDto.create(user.getUsername(), user.getRole().name());
        return new CustomOAuth2User(tokenDto);
    }
}