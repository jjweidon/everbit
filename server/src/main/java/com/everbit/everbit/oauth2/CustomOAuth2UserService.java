package com.everbit.everbit.oauth2;

import com.everbit.everbit.dto.CustomOAuth2User;
import com.everbit.everbit.dto.MemberDto;
import com.everbit.everbit.entity.Member;
import com.everbit.everbit.service.MemberService;
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
            log.info("OAuth2 사용자 정보 로드 시작 - 공급자: {}", userRequest.getClientRegistration().getRegistrationId());
            log.info("클라이언트 등록 정보: id={}, redirectUri={}", 
                    userRequest.getClientRegistration().getClientId(),
                    userRequest.getClientRegistration().getRedirectUri());
            
            log.info("액세스 토큰: {}, 값: {}", 
                    userRequest.getAccessToken().getTokenType(),
                    userRequest.getAccessToken().getTokenValue().substring(0, Math.min(10, userRequest.getAccessToken().getTokenValue().length())) + "...");
            
            OAuth2User oAuth2User = super.loadUser(userRequest);
            log.info("OAuth2User 로드 완료");
            
            // 속성 정보 로깅 (민감 정보는 제한적으로 로깅)
            Map<String, Object> attributes = oAuth2User.getAttributes();
            log.debug("OAuth2User 속성: {}", attributes);
            
            // ID 값만 별도 로깅
            if (attributes.containsKey("id")) {
                log.info("카카오 사용자 ID: {}", attributes.get("id"));
            } else {
                log.warn("카카오 사용자 ID가 없습니다.");
            }
            
            // 프로필 정보 확인
            if (attributes.containsKey("kakao_account")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                if (kakaoAccount.containsKey("profile")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                    log.info("카카오 프로필: nickname={}", profile.get("nickname"));
                } else {
                    log.warn("카카오 프로필 정보가 없습니다.");
                }
            } else {
                log.warn("카카오 계정 정보가 없습니다.");
            }
            
            Member member = memberService.createMember(oAuth2User);
            log.info("사용자 생성/조회 완료 - ID: {}, username: {}", member.getId(), member.getUsername());
            
            MemberDto memberDto = MemberDto.from(member);
            log.info("OAuth2 사용자 정보 로드 완료");
            return new CustomOAuth2User(memberDto);
        } catch (Exception e) {
            log.error("OAuth2 사용자 정보 로드 중 오류 발생", e);
            throw e;
        }
    }
}