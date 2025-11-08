package org.example.gyeonggi_partners.common.exception;

import lombok.Getter;

public class MessageException extends RuntimeException {

    @Getter
    private final ErrorCode errorCode;
    public MessageException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
