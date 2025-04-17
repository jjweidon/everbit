package com.everbit.everbit.upbit.exception;

import com.everbit.everbit.member.entity.Member;

public class AccountException extends RuntimeException {
    public AccountException(String message) {
        super(message);
    }

    public AccountException(String accountId, String message) {
        super("AccountID: " + accountId + " - " + message);
    }

    public static AccountException noAccountMember(Member member) {
        return new AccountException("MemberID: " + member.getId() + " - 계좌를 가지고 있지 않은 사용자입니다.");
    }
}
