package com.everbit.everbit.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.everbit.everbit.global.util.EncryptionUtil;
import com.everbit.everbit.user.dto.UpbitKeyRequest;
import com.everbit.everbit.user.dto.UpbitApiKeysResponse;
import com.everbit.everbit.user.dto.UserResponse;
import com.everbit.everbit.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserManager {
    private final UserService userService;
    private final EncryptionUtil encryptionUtil;
    
    @Transactional(readOnly = true)
    public UserResponse getUserResponse(String username) {
        User user = userService.findUserByUsername(username);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse registerUpbitApiKeys(String username, UpbitKeyRequest request) {
        User user = userService.findUserByUsername(username);
        String encryptedAccessKey = encryptionUtil.encrypt(request.accessKey());
        String encryptedSecretKey = encryptionUtil.encrypt(request.secretKey());
        user.updateKeys(encryptedAccessKey, encryptedSecretKey);
        userService.saveUser(user);
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UpbitApiKeysResponse getUpbitApiKeys(String username) {
        User user = userService.findUserByUsername(username);
        return UpbitApiKeysResponse.from(user);
    }

    @Transactional
    public void updateEmail(String username, String email) {
        User user = userService.findUserByUsername(username);
        user.updateEmail(email);
        userService.saveUser(user);
    }
}
