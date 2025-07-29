package com.everbit.everbit.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.everbit.everbit.global.util.EncryptionUtil;
import com.everbit.everbit.user.dto.BotSettingRequest;
import com.everbit.everbit.user.dto.BotSettingResponse;
import com.everbit.everbit.user.dto.UpbitKeyRequest;
import com.everbit.everbit.user.dto.UpbitApiKeysResponse;
import com.everbit.everbit.user.dto.UserResponse;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.entity.BotSetting;

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
        String decryptedAccessKey = encryptionUtil.decrypt(user.getUpbitAccessKey());
        String decryptedSecretKey = encryptionUtil.decrypt(user.getUpbitSecretKey());
        return UpbitApiKeysResponse.of(decryptedAccessKey, decryptedSecretKey);
    }

    @Transactional
    public void updateEmail(String username, String email) {
        User user = userService.findUserByUsername(username);
        user.updateEmail(email);
        userService.saveUser(user);
    }

    @Transactional
    public UserResponse toggleBotActive(String username) {
        User user = userService.findUserByUsername(username);
        user.toggleBotActive();
        userService.saveUser(user);
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public BotSettingResponse getBotSetting(String username) {
        User user = userService.findUserByUsername(username);
        return BotSettingResponse.from(user.getBotSetting());
    }

    @Transactional
    public BotSettingResponse updateBotSetting(String username, BotSettingRequest request) {
        User user = userService.findUserByUsername(username);
        BotSetting botSetting = user.getBotSetting();
        botSetting.update(request);
        userService.saveUser(user);
        return BotSettingResponse.from(botSetting);
    }
}
