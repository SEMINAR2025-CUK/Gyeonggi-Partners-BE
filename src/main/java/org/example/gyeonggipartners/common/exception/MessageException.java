package org.example.gyeonggipartners.common.exception;

import lombok.Getter;

public class MessageException extends RuntimeException {

    @Getter
    private final transient ErrorCode errorCode;
    public MessageException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
