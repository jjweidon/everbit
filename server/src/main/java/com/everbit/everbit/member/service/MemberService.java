package com.everbit.everbit.member.service;

import com.everbit.everbit.member.entity.Member;
import com.everbit.everbit.member.entity.enums.Role;
import com.everbit.everbit.member.exception.MemberException;
import com.everbit.everbit.oauth2.dto.KakaoResponse;
import com.everbit.everbit.oauth2.dto.OAuth2Response;
import com.everbit.everbit.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.everbit.everbit.member.dto.MemberResponse;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final String encryptionPassword = "your-secure-password"; // 실제 운영에서는 환경변수로 관리
    private final String encryptionSalt = "your-salt"; // 실제 운영에서는 환경변수로 관리

    public Member createMember(OAuth2User oAuth2Member) {
        OAuth2Response oAuth2Response = new KakaoResponse(oAuth2Member.getAttributes());
        String username = oAuth2Response.getName()+" "+oAuth2Response.getProviderId();
        
        // 이미 존재하는 사용자는 새로 생성하지 않고 기존 정보 반환
        Optional<Member> existingMember = memberRepository.findByUsername(username);
        if (existingMember.isPresent()) {
            log.info("기존 사용자 로그인: {}", username);
            return existingMember.get();
        }

        // 새로운 사용자 생성
        log.info("새로운 사용자 등록: {}", username);
        Member member = Member.builder()
                .username(username)
                .image(oAuth2Response.getImage())
                .role(Role.ROLE_USER)
                .build();
        return memberRepository.save(member);
    }

    public MemberResponse getMemberResponse(String username) {
        Member member = findMemberByUsername(username);
        return MemberResponse.from(member);
    }

    public void saveUpbitApiKeys(String memberId, String accessKey, String secretKey) {
        Member member = findMemberByMemberId(memberId);
        
        TextEncryptor encryptor = Encryptors.text(encryptionPassword, encryptionSalt);
        String encryptedAccessKey = encryptor.encrypt(accessKey);
        String encryptedSecretKey = encryptor.encrypt(secretKey);
        
        member.setUpbitAccessKey(encryptedAccessKey);
        member.setUpbitSecretKey(encryptedSecretKey);
        
        memberRepository.save(member);
    }

    public Member findMemberByMemberId(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> MemberException.notFound(memberId));
    }

    public Member findMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> MemberException.notFound(username));
    }
}
