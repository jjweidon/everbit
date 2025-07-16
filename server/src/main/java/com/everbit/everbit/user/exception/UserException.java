package com.everbit.everbit.user.exception;

public class UserException extends RuntimeException {
    public UserException(String message) {
        super(message);
    }

    public UserException(String userId, String message) {
        super("UserID: " + userId + " - " + message);
    }

    public static UserException notFound(String userId) {
        return new UserException(userId, "존재하지 않는 사용자 아이디입니다.");
    }

    public static UserException notFoundByUsername(String username) {
        return new UserException(username, "존재하지 않는 사용자 이름입니다.");
    }

    public static UserException upbitApiKeyRequired() {
        return new UserException("업비트 API 키 등록이 필요합니다.");
    }
}
