package com.everbit.everbit.member.exception;

public class MemberException extends RuntimeException {
    public MemberException(String message) {
        super(message);
    }

    public MemberException(String memberId, String message) {
        super("MemberID: " + memberId + " - " + message);
    }

    public static MemberException notFound(String memberId) {
        return new MemberException(memberId, "존재하지 않는 사용자 아이디입니다.");
    }

    public static MemberException upbitApiKeyRequired() {
        return new MemberException("업비트 API 키 등록이 필요합니다.");
    }
}
