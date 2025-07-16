package com.everbit.everbit.upbit.service;

import com.everbit.everbit.upbit.entity.Account;
import com.everbit.everbit.upbit.exception.AccountException;

import org.springframework.stereotype.Service;

import com.everbit.everbit.user.dto.UpbitKeyRequest;
import com.everbit.everbit.user.dto.UserResponse;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountManager {
    private final AccountService accountService;
    private final UserService userService;

    public UserResponse createAccount(String username, UpbitKeyRequest request) {
        User user = userService.findUserByUsername(username);
        if (accountService.isAccountExists(user)) {
            throw AccountException.alreadyExists(user);
        }
        Account account = Account.of(user, request.accessKey(), request.secretKey());
        accountService.saveAccount(account);

        return UserResponse.from(user);
    }
}
