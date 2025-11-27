package org.example.gyeonggipartners.domain.proposal.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.gyeonggipartners.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ProposalErrorCode implements ErrorCode {

    // 기본 CRUD
    PROPOSAL_NOT_FOUND(404, "P001", "존재하지 않는 제안서입니다."),

    // 락(Lock) & 편집 관련
    PROPOSAL_BEING_EDITED(409, "P002", "다른 사용자가 현재 제안서를 수정 중입니다."), // 진입 시도 시
    NOT_LOCK_OWNER(403, "P004", "편집 권한이 없습니다. (락 소유자가 아님)"), // 저장/완료 시도 시
    LOCK_EXPIRED(403, "P005", "편집 세션이 만료되었습니다. 다시 시도해주세요."),

    //상태별 수정 불가 에러
    CANNOT_EDIT_IN_VOTING(400, "P020", "현재 투표 진행 중이라 수정할 수 없습니다."),       // VOTING
    CANNOT_EDIT_COMPLETED(400, "P021", "확정된 제안서는 수정할 수 없습니다."),               // COMPLETED
    CANNOT_EDIT_READY_TO_SUBMIT(400, "P022", "제출 대기(제출 직전) 상태라 수정할 수 없습니다."), // READY_TO_SUBMIT
    CANNOT_EDIT_SUBMITTED(400, "P023", "이미 제출된 제안서는 수정할 수 없습니다."),

    // 상태 관련
    NOT_IN_DRAFTING(400, "P006", "작성 중인 상태가 아닙니다."),
    NOT_IN_VOTING(400, "P009", "투표 중인 제안서가 아닙니다."),
    ALREADY_VOTING(409, "P012", "이미 투표가 진행 중입니다."),
    ALREADY_SUBMITTABLE(409, "P013", "이미 제출 가능한 상태입니다."),
    ALREADY_CONSENTED(409, "P003", "이미 동의한 제안서입니다."),

    // 유효성 검사
    INVALID_TITLE_LENGTH(400, "P010", "제목 길이는 5자 이상 100자 이하여야 합니다."),
    PROPOSAL_LIMIT_EXCEEDED(400, "P016", "논의방당 최대 5개의 제안서만 생성할 수 있습니다."),
    UNAUTHORIZED_ACCESS(403, "P008", "해당 제안서에 대한 권한이 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}