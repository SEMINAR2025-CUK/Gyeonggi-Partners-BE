package org.example.gyeonggi_partners.domain.message.exception;


import org.example.gyeonggi_partners.common.exception.ErrorCode;

public enum MessageErrorCode implements ErrorCode {


    MESSAGE_CONTENT_EMPTY(400, "M001", "메시지 내용을 입력해주세요."),
    MESSAGE_TOO_LONG(400, "M002", "메시지는 2000자 이하로 입력해주세요."),
    NOT_A_ROOM_MEMBER(403, "M003", "해당 논의방의 멤버가 아닙니다.");

    MessageErrorCode (int status, String code, String message){
        this.status = status;
        this.code = code;
        this.message = message;
    }

    private final int status;
    private final String code;
    private final String message;

    @Override
    public int getStatus() {
        return this.status;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
