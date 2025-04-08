package com.everbit.everbit.exception;

public class MemberException extends RuntimeException {
    public MemberException(String memberId, String message) {
        super("Id: " + memberId + " - " + message);
    }

    public static MemberException notFound(String memberId) {
        return new MemberException(memberId, "User를 찾을 수 없습니다.");
    }

    public static MemberException alreadyExists(String username) {
        return new MemberException(null, "username: " + username + " - 이미 존재하는 사용자 이름입니다.");
    }
}
