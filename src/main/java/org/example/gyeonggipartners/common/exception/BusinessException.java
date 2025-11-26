package org.example.gyeonggipartners.common.exception;


import lombok.Getter;

public class BusinessException extends RuntimeException {

    @Getter
    private final transient ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}