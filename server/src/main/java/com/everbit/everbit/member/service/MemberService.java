package com.everbit.everbit.member.service;

import com.everbit.everbit.member.dto.UpbitApiKeyRequest;
import com.everbit.everbit.member.entity.Member;
import com.everbit.everbit.member.entity.enums.Role;
import com.everbit.everbit.member.exception.MemberException;
import com.everbit.everbit.oauth2.dto.KakaoResponse;
import com.everbit.everbit.oauth2.dto.OAuth2Response;
import com.everbit.everbit.member.repository.MemberRepository;
import com.everbit.everbit.upbit.entity.Account;
import com.everbit.everbit.upbit.exception.AccountException;
import com.everbit.everbit.upbit.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.everbit.everbit.member.dto.MemberResponse;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;

    @Value("${upbit.encryption.password}")
    private String encryptionPassword;
    @Value("${upbit.encryption.salt}")
    private String encryptionSalt;

    @Transactional
    public Member createMember(OAuth2User oAuth2Member) {
        OAuth2Response oAuth2Response = new KakaoResponse(oAuth2Member.getAttributes());
        String username = oAuth2Response.getName() + oAuth2Response.getProviderId();
        
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

    @Transactional(readOnly = true)
    public MemberResponse getMemberResponse(String memberId) {
        Member member = findMemberByMemberId(memberId);
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse saveUpbitApiKeys(String memberId, UpbitApiKeyRequest request) {
        Member member = findMemberByMemberId(memberId);
        Account account = findAccountByMember(member);
        
        TextEncryptor encryptor = Encryptors.text(encryptionPassword, encryptionSalt);
        String encryptedAccessKey = encryptor.encrypt(request.accessKey());
        String encryptedSecretKey = encryptor.encrypt(request.secretKey());
        
        account.setUpbitAccessKey(encryptedAccessKey);
        account.setUpbitSecretKey(encryptedSecretKey);
        
        accountRepository.save(account);
        member.setUpbitConnected(true);
        memberRepository.save(member);
        return MemberResponse.from(member);
    }

    public Member findMemberByMemberId(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> MemberException.notFound(memberId));
    }

    public Member findMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> MemberException.notFound(username));
    }

    public Account findAccountByMember(Member member) {
        return accountRepository.findByMember(member)
                .orElseThrow(() -> AccountException.noAccountMember(member));
    }
}
