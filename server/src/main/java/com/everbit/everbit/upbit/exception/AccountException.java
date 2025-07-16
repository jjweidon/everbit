package com.everbit.everbit.upbit.exception;

import com.everbit.everbit.user.entity.User;

public class AccountException extends RuntimeException {
    public AccountException(String message) {
        super(message);
    }

    public AccountException(String accountId, String message) {
        super("AccountID: " + accountId + " - " + message);
    }

    public static AccountException noAccount(User user) {
        return new AccountException("UserID: " + user.getId() + " - 계좌를 가지고 있지 않은 사용자입니다.");
    }

    public static AccountException alreadyExists(User user) {
        return new AccountException("UserID: " + user.getId() + " - 이미 계좌를 가지고 있는 사용자입니다.");
    }
}
