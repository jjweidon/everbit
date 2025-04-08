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

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberService memberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        log.info("OAuth2 로그인 진행 중 - 공급자: {}", userRequest.getClientRegistration().getRegistrationId());
        log.debug("OAuth2User 속성: {}", oAuth2User.getAttributes());

        Member member = memberService.createMember(oAuth2User);
        log.info("사용자 생성/조회 완료 - ID: {}, username: {}", member.getId(), member.getUsername());
        
        MemberDto memberDto = MemberDto.from(member);
        return new CustomOAuth2User(memberDto);
    }
}