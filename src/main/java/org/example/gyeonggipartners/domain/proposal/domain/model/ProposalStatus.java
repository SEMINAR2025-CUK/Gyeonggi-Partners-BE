package org.example.gyeonggipartners.domain.proposal.domain.model;

public enum ProposalStatus {

    DRAFTING,         // 내용 작성 중 (Locked)
    SAVING,           // 미 완성 상태 (unLocked)
    COMPLETED,        // 완성 상태 (Locked)
    VOTING,           // 동의 진행 중 (Locked)
    READY_TO_SUBMIT,  // 제출 가능 (Locked)
    SUBMITTED         // 외부 시스템에 제출 완료 (unLocked)
}
