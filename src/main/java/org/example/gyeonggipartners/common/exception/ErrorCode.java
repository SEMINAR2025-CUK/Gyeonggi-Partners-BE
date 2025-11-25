package org.example.gyeonggipartners.common.exception;

public interface ErrorCode {
    int getStatus();
    String getCode();
    String getMessage();
}