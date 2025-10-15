package org.example.gyeonggi_partners.domain.proposal.exception;

import org.example.gyeonggi_partners.common.exception.ErrorCode;

public enum ProposalErrorCode implements ErrorCode {

    /**
     * 더 정의하거나 삭제해야할 상수 있는지 체크해볼것.
     * */

    // Enum 상수 정의
    PROPOSAL_NOT_FOUND(404, "P001", "존재하지 않는 제안서입니다."),
    PROPOSAL_BEING_EDITED(409, "P002", "다른 사용자가 현재 제안서를 수정 중입니다."),
    ALREADY_CONSENTED(409, "P003", "이미 동의한 제안서입니다."),
    PROPOSAL_LOCKED(409, "P004", "현재 동의가 진행 중인 제안서는 수정할 수 없습니다."),
    EDIT_CONFLICT(409, "P005", "다른 사용자에 의해 문서가 수정되었습니다. 페이지를 새로고침 해주세요.");

    private final int status;
    private final String code;
    private final String message;

    ProposalErrorCode (int status, String code, String message){
        this.status = status;
        this.code = code;
        this.message = message;
    }

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
