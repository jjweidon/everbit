package com.everbit.everbit.oauth2.service;

import com.everbit.everbit.oauth2.dto.CustomOAuth2User;
import com.everbit.everbit.member.dto.MemberDto;
import com.everbit.everbit.member.entity.Member;
import com.everbit.everbit.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberService memberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            log.info("OAuth2 사용자 정보 로드 - 공급자: {}", userRequest.getClientRegistration().getRegistrationId());
            
            OAuth2User oAuth2User = super.loadUser(userRequest);
            
            // 카카오 사용자 ID 로깅
            Map<String, Object> attributes = oAuth2User.getAttributes();
            if (attributes.containsKey("id")) {
                log.info("카카오 사용자 ID: {}", attributes.get("id"));
            }
            
            // 프로필 정보 확인
            if (attributes.containsKey("kakao_account")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                
                if (kakaoAccount.containsKey("profile")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                    log.info("카카오 프로필: nickname={}", profile.get("nickname"));
                }
            }
            
            Member member = memberService.createMember(oAuth2User);
            log.info("사용자 생성/조회 완료 - ID: {}, username: {}", member.getId(), member.getUsername());
            
            MemberDto memberDto = MemberDto.from(member);
            return new CustomOAuth2User(memberDto);
        } catch (Exception e) {
            log.error("OAuth2 사용자 정보 로드 중 오류 발생", e);
            throw e;
        }
    }
}