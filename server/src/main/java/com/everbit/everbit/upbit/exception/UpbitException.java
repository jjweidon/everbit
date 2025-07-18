package com.everbit.everbit.upbit.exception;

public class UpbitException extends RuntimeException {
    public UpbitException(String message) {
        super(message);
    }

    public UpbitException(String message, Throwable cause) {
        super(message, cause);
    }
} 