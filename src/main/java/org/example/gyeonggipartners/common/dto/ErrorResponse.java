package org.example.gyeonggipartners.common.dto;


import org.example.gyeonggipartners.common.exception.ErrorCode;

public record ErrorResponse(String message) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getMessage());
    }
}