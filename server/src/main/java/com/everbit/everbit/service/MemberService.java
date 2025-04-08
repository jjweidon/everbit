package com.everbit.everbit.service;

import com.everbit.everbit.entity.Member;
import com.everbit.everbit.entity.enums.Role;
import com.everbit.everbit.exception.MemberException;
import com.everbit.everbit.oauth2.KakaoResponse;
import com.everbit.everbit.oauth2.OAuth2Response;
import com.everbit.everbit.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public Member createMember(OAuth2User oAuth2Member) {
        OAuth2Response oAuth2Response = new KakaoResponse(oAuth2Member.getAttributes());
        String username = oAuth2Response.getName()+" "+oAuth2Response.getProviderId();
        if (memberRepository.findByUsername(username).isPresent()) {
            throw MemberException.alreadyExists(username);
        }
        Member member = Member.builder()
                .username(username)
                .image(oAuth2Response.getImage())
                .role(Role.ROLE_USER)
                .build();
        return memberRepository.save(member);
    }

    public Member getMemberByMemberId(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> MemberException.notFound(memberId));
    }

    public Member getMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> MemberException.notFound(username));
    }
}
