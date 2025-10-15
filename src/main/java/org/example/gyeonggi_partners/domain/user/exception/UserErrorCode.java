package org.example.gyeonggi_partners.domain.user.exception;


import org.example.gyeonggi_partners.common.exception.ErrorCode;

public enum UserErrorCode implements ErrorCode {

    // 회원가입 관련
    DUPLICATE_LOGIN_ID(409, "U001", "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL(409, "U002", "이미 사용 중인 이메일입니다."),
    DUPLICATE_PHONE_NUMBER(409, "U003", "이미 등록된 전화번호입니다."),

    // 이메일 인증 관련
    INVALID_VERIFICATION_CODE(400, "C001", "인증번호가 올바르지 않거나 만료되었습니다."),

    // 로그인 관련
    LOGIN_FAILED(401, "A001", "아이디 또는 비밀번호가 일치하지 않습니다.");

    UserErrorCode(int status, String code, String message){
        this.status = status;
        this.code = code;
        this.message = message;
    }

    private final int status;
    private final String code;
    private final String message;

    @Override
    public int getStatus() {
        return this.status; // 0 대신 현재 객체의 status 필드를 반환합니다.
    }

    @Override
    public String getCode() { // ErrorCode 인터페이스에 getCode()가 있다고 가정
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message; // "" 대신 현재 객체의 message 필드를 반환합니다.
    }
}
