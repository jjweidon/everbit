package com.everbit.everbit.user.service;

import com.everbit.everbit.oauth2.dto.KakaoResponse;
import com.everbit.everbit.oauth2.dto.OAuth2Response;
import com.everbit.everbit.upbit.entity.Account;
import com.everbit.everbit.upbit.exception.AccountException;
import com.everbit.everbit.upbit.repository.AccountRepository;
import com.everbit.everbit.user.dto.UserResponse;
import com.everbit.everbit.user.dto.UpbitApiKeyRequest;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.entity.enums.Role;
import com.everbit.everbit.user.exception.UserException;
import com.everbit.everbit.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
public class UserService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Value("${upbit.encryption.password}")
    private String encryptionPassword;
    @Value("${upbit.encryption.salt}")
    private String encryptionSalt;

    @Transactional
    public User createUser(OAuth2User oAuth2User) {
        OAuth2Response oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        String username = oAuth2Response.getName() + oAuth2Response.getProviderId();
        
        // 이미 존재하는 사용자는 새로 생성하지 않고 기존 정보 반환
        Optional<User> existinguser = userRepository.findByUsername(username);
        if (existinguser.isPresent()) {
            log.info("기존 사용자 로그인: {}", username);
            return existinguser.get();
        }

        // 새로운 사용자 생성
        log.info("새로운 사용자 등록: {}", username);
        User user = User.builder()
                .username(username)
                .image(oAuth2Response.getImage())
                .role(Role.ROLE_USER)
                .build();
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserResponse(String userId) {
        User user = findUserByUserId(userId);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse saveUpbitApiKeys(String userId, UpbitApiKeyRequest request) {
        User user = findUserByUserId(userId);
        Account account = findAccountByUser(user);
        
        TextEncryptor encryptor = Encryptors.text(encryptionPassword, encryptionSalt);
        String encryptedAccessKey = encryptor.encrypt(request.accessKey());
        String encryptedSecretKey = encryptor.encrypt(request.secretKey());
        
        account.setUpbitAccessKey(encryptedAccessKey);
        account.setUpbitSecretKey(encryptedSecretKey);
        
        accountRepository.save(account);
        user.setUpbitConnected(true);
        userRepository.save(user);
        return UserResponse.from(user);
    }

    public User findUserByUserId(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> UserException.notFound(userId));
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> UserException.notFound(username));
    }

    public Account findAccountByUser(User user) {
        return accountRepository.findByUser(user)
                .orElseThrow(() -> AccountException.noAccount(user));
    }
}
