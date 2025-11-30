package org.example.gyeonggipartners.domain.proposal.domain.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Proposal {

    public static final int LOCK_TIMEOUT_MINUTES = 45;

    private Long id;
    private Long roomId;
    private Long lastModifierId;

    private String title;
    private String problemOverview;
    private String solution;
    private List<Evidence> evidences;

    private Integer requiredConsents;
    private LocalDateTime consentDeadline;
    private ProposalStatus status;

    private Long lockedBy;
    private LocalDateTime lockedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;


    /**
     * 기존 제안서 복원 (DB에서 조회용 Factory Method)
     */
    public static Proposal restore(Long id, Long roomId, Long lastModifierId,
                                   String title, String problemOverview, String solution,
                                   List<Evidence> evidences, Integer requiredConsents,
                                   LocalDateTime consentDeadline, ProposalStatus status,
                                   Long lockedBy, LocalDateTime lockedAt,
                                   LocalDateTime createdAt, LocalDateTime updatedAt,
                                   LocalDateTime deletedAt) {
        return Proposal.builder()
                .id(id)
                .roomId(roomId)
                .lastModifierId(lastModifierId)
                .title(title)
                .problemOverview(problemOverview)
                .solution(solution)
                .evidences(evidences != null ? evidences : new ArrayList<>())
                .requiredConsents(requiredConsents)
                .consentDeadline(consentDeadline)
                .status(status)
                .lockedBy(lockedBy)
                .lockedAt(lockedAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .build();
    }


    // =================================================================
    // 1. [생성] Create Phase
    // =================================================================

    /**
     * 제안서 생성
     * - 생성 직후 작성자가 락을 소유하며 DRAFTING 상태가 됨
     */
    public static Proposal create(Long roomId, Long authorId, String title,
                                  String problemOverview, String solution,
                                  List<Evidence> evidences) {
        validateContent(title, problemOverview, solution);

        return Proposal.builder()
                .roomId(roomId)
                .lastModifierId(authorId)
                .title(title)
                .problemOverview(problemOverview)
                .solution(solution)
                .evidences(evidences != null ? evidences : new ArrayList<>())
                .status(ProposalStatus.DRAFTING)
                .lockedBy(authorId)
                .lockedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // =================================================================
    // 2. [편집] Editing Phase (생성 -> 수정 -> 임시저장 -> 완료)
    // =================================================================

    /**
     * 편집 모드 진입 (락 획득 & 상태 전환)
     * - 시나리오: B가 SAVING 상태의 제안서에 접근 -> DRAFTING 전환, 수정자 B로 갱신
     */
    public void startEditing(Long userId) {
        if (this.status != ProposalStatus.SAVING) {
            throw new IllegalStateException("작성 중(임시저장)인 제안서만 편집할 수 있습니다.");
        }

        validateAcquirableLock(userId);
        acquireLock(userId);
        transitionToDrafting(userId);
    }

    /**
     * 내용 수정 (쓰기 작업)
     * - 시나리오: 락을 가진 사용자가 내용을 타이핑함
     */
    public void updateContent(Long userId, String title, String problemOverview,
                              String solution, List<Evidence> evidences) {
        validateLockOwner(userId);
        validateContent(title, problemOverview, solution);

        this.title = title;
        this.problemOverview = problemOverview;
        this.solution = solution;
        this.evidences = evidences != null ? evidences : new ArrayList<>();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 임시 저장 (편집 중단)
     * - 시나리오: 작성자가 '저장' 버튼을 누르고 나감 (SAVING 상태로 전환, 락 해제)
     */
    public void saveAsDraft(Long userId) {
        validateLockOwner(userId);

        this.status = ProposalStatus.SAVING;
        releaseLock();
    }

    /**
     * 작성 완료 (확정)
     * - 시나리오: 작성자가 '완료' 버튼을 누름 (COMPLETED 상태로 전환, 락 해제)
     */
    public void complete(Long userId) {
        validateLockOwner(userId);

        this.status = ProposalStatus.COMPLETED;
        releaseLock();
    }

    // =================================================================
    // 3. [투표] Voting Phase (완료 -> 투표 시작 -> 투표 종료)
    // =================================================================

    /**
     * 투표 시작
     * - 시나리오: 완료 모달에서 인원수/마감일을 입력하고 확인
     */
    public void startVoting(Integer requiredConsents, LocalDateTime consentDeadline) {
        validateStatus(ProposalStatus.COMPLETED, "확정된 제안서만 투표를 시작할 수 있습니다.");
        validateVotingParams(requiredConsents, consentDeadline);

        this.requiredConsents = requiredConsents;
        this.consentDeadline = consentDeadline;
        this.status = ProposalStatus.VOTING;
    }

    /**
     * 투표 종료 조건 체크 및 상태 변경
     * - 시나리오: 동의 인원이 찼거나, 마감기한이 지났으면 제출 대기 상태로 변경
     */
    public void finishVoting(int currentConsentCount) {
        validateStatus(ProposalStatus.VOTING, "투표 진행 중인 제안서가 아닙니다.");

        // 투표 종료 조건: 최소 인원 충족 또는 마감 시간 경과
        if (!canFinishVoting(currentConsentCount)) {
            throw new IllegalStateException("투표 종료 조건(최소 인원 충족 또는 마감 시간 경과)을 만족하지 않았습니다.");
        }

        this.status = ProposalStatus.READY_TO_SUBMIT;
    }

    // =================================================================
    // 4. [제출] Submit Phase
    // =================================================================

    /**
     * 제출 - READY_TO_SUBMIT → SUBMITTED 상태 전환
     */
    public void submit() {
        validateStatus(ProposalStatus.READY_TO_SUBMIT, "제출 대기 상태인 제안서만 제출할 수 있습니다.");
        this.status = ProposalStatus.SUBMITTED;
    }

    // =================================================================
    // Internal Helper Methods
    // =================================================================

    // --- 검증(Validation) 관련 ---

    /**
     * 제안서 내용 검증 - title 5자 이상, problemOverview/solution 필수
     */
    private static void validateContent(String title, String problem, String solution) {
        if (title == null || title.isBlank() || title.length() < 5)
            throw new IllegalArgumentException("제목은 5자 이상이어야 합니다.");
        if (problem == null || problem.isBlank())
            throw new IllegalArgumentException("문제 개요는 필수입니다.");
        if (solution == null || solution.isBlank())
            throw new IllegalArgumentException("해결방안은 필수입니다.");
    }

    /**
     * 현재 상태가 expected와 일치하는지 검증
     */
    private void validateStatus(ProposalStatus expected, String message) {
        if (this.status != expected) throw new IllegalStateException(message);
    }

    /**
     * 투표 파라미터 검증 - count 1명 이상, deadline 미래 시점
     */
    private void validateVotingParams(Integer count, LocalDateTime deadline) {
        if (count == null || count < 1) throw new IllegalArgumentException("최소 동의 인원은 1명 이상입니다.");
        if (deadline == null || deadline.isBefore(LocalDateTime.now())) throw new IllegalArgumentException("마감일은 미래여야 합니다.");
    }

    // --- 락(Lock) 관련 ---

    /**
     * 락 획득 가능 여부 검증 - 락이 없거나, 만료됐거나, 내 락이면 통과
     */
    private void validateAcquirableLock(Long userId) {
        // 이미 내가 락을 가지고 있거나, 락이 없거나, 락이 만료되었으면 OK
        boolean isLockFree = (this.lockedBy == null) || isLockExpired();
        boolean isMyLock = (this.lockedBy != null && this.lockedBy.equals(userId));

        if (!isLockFree && !isMyLock) {
            throw new IllegalStateException("현재 다른 사용자가 편집 중입니다.");
        }
    }

    /**
     * 락 소유자 검증 및 세션 갱신 - 락 소유자가 아니거나 만료 시 예외
     */
    private void validateLockOwner(Long userId) {
        if (this.lockedBy == null || !this.lockedBy.equals(userId)) {
            throw new IllegalStateException("편집 권한(락)이 없습니다.");
        }
        if (isLockExpired()) {
            throw new IllegalStateException("편집 세션이 만료되었습니다. 다시 진입해주세요.");
        }
        this.lockedAt = LocalDateTime.now();
    }

    /**
     * 락 획득 - lockedBy, lockedAt 갱신
     */
    private void acquireLock(Long userId) {
        this.lockedBy = userId;
        this.lockedAt = LocalDateTime.now();
    }

    /**
     * 락 해제 - lockedBy, lockedAt을 null로 초기화
     */
    private void releaseLock() {
        this.lockedBy = null;
        this.lockedAt = null;
    }

    /**
     * 락 만료 판단 - lockedAt 기준 LOCK_TIMEOUT_MINUTES(45분) 경과 여부
     */
    private boolean isLockExpired() {
        if (this.lockedAt == null) return true;
        return LocalDateTime.now().isAfter(this.lockedAt.plusMinutes(LOCK_TIMEOUT_MINUTES));
    }

    // --- 상태 전이 관련 ---

    /**
     * SAVING → DRAFTING 전환 및 수정자 변경
     * - DRAFTING 상태에서 호출 시 무시됨
     */
    private void transitionToDrafting(Long userId) {
        if (this.status == ProposalStatus.SAVING) {
            this.status = ProposalStatus.DRAFTING;
            this.lastModifierId = userId;
        }
        // 이미 DRAFTING인데 작성자가 아닌 사람이 들어오는 경우는 validateAcquirableLock에서 막힘
    }

    // --- 투표 조건 판단 ---

    /**
     * 투표 종료 가능 여부 - 최소 인원 충족 또는 마감 시간 경과
     */
    private boolean canFinishVoting(int currentCount) {
        boolean isQuotaMet = currentCount >= this.requiredConsents;
        boolean isExpired = LocalDateTime.now().isAfter(this.consentDeadline);

        return isQuotaMet || isExpired;
    }
}
