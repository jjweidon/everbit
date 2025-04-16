package com.everbit.everbit.exception;

import lombok.Getter;

@Getter
public class MemberException extends RuntimeException {
    private final String code;

    public MemberException(String message, String code) {
        super(message);
        this.code = code;
    }

    public static MemberException notFound(String id) {
        return new MemberException("Member not found with id: " + id, "MEMBER_NOT_FOUND");
    }

    public static MemberException upbitApiKeyRequired() {
        return new MemberException("업비트 API 키 등록이 필요합니다.", "UPBIT_API_KEY_REQUIRED");
    }

    public static MemberException alreadyExists(String username) {
        return new MemberException(null, "username: " + username + " - 이미 존재하는 사용자 이름입니다.");
    }
}
