package com.everbit.everbit.user.service;

import com.everbit.everbit.oauth2.dto.KakaoResponse;
import com.everbit.everbit.oauth2.dto.OAuth2Response;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.exception.UserException;
import com.everbit.everbit.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User createOrFindUser(OAuth2User oAuth2User) {
        OAuth2Response oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        String username = oAuth2Response.getProvider() + "-" + oAuth2Response.getProviderId();

        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    log.info("새로운 사용자 등록: {}", username);
                    User newUser = User.init(oAuth2Response);
                    return userRepository.save(newUser);
                });
    }

    public User findUserByUserId(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> UserException.notFound(userId));
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> UserException.notFoundByUsername(username));
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }
}
