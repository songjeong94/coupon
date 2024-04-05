package com.example.couponcore.exception;

import lombok.Getter;

@Getter
public class CouponIssueException extends RuntimeException{

    private final ErrorCode errorCode;
    private final String message;

    public CouponIssueException(ErrorCode errorCode, String messages) {
        this.errorCode = errorCode;
        this.message = messages;
    }

    @Override
    public String getMessage() {
        return "[%s] %s".formatted(errorCode, message);
    }
}
