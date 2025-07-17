package com.everbit.everbit.upbit.service;

import com.everbit.everbit.upbit.entity.Account;
import org.springframework.stereotype.Service;
import com.everbit.everbit.user.dto.UpbitKeyRequest;
import com.everbit.everbit.user.dto.UserResponse;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.service.UserService;
import com.everbit.everbit.global.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountManager {
    private final AccountService accountService;
    private final UserService userService;
    private final EncryptionUtil encryptionUtil;

    public UserResponse registerUpbitApiKeys(String username, UpbitKeyRequest request) {
        User user = userService.findUserByUsername(username);
        String encryptedAccessKey = encryptionUtil.encrypt(request.accessKey());
        String encryptedSecretKey = encryptionUtil.encrypt(request.secretKey());

        Account account = accountService.findOrCreateAccount(user);
        account.updateKeys(encryptedAccessKey, encryptedSecretKey);
        accountService.saveAccount(account);
        
        user.connectUpbit();
        userService.saveUser(user);

        return UserResponse.from(user);
    }
}
