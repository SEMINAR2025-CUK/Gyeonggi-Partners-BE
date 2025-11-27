package org.example.gyeonggipartners.domain.proposal.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.gyeonggipartners.domain.proposal.api.dto.*;
import org.example.gyeonggipartners.domain.proposal.application.ProposalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/proposals")
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;

    // =================================================================
    // 1. 조회 API (Read - Open to all room members)
    // =================================================================

    /**
     * 논의방 내 제안서 목록 조회
     * - [정책] 해당 방의 멤버라면 누구나 조회 가능
     */
    @GetMapping
    public ResponseEntity<List<ProposalResponse>> getProposalsByRoom(
            @RequestParam Long roomId,
            @RequestHeader("X-USER-ID") Long userId
    ) {
        // Service 시그니처: (userId, roomId)
        List<ProposalResponse> responses = proposalService.getProposalsByRoom(userId, roomId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 제안서 단건 상세 조회
     * - [정책] 제한 없음 (작성 중인 상태라도 내용 조회 가능)
     * - 프론트엔드는 응답의 lockedBy 필드를 확인하여 '수정 버튼' 활성화 여부를 결정해야 함
     */
    @GetMapping("/{proposalId}")
    public ResponseEntity<ProposalResponse> getProposal(
            @PathVariable Long proposalId,
            @RequestHeader("X-USER-ID") Long userId
    ) {
        // Service 시그니처: (userId, proposalId)
        ProposalResponse response = proposalService.getProposal(userId, proposalId);
        return ResponseEntity.ok(response);
    }

    /**
     * 동의자 목록 조회
     */
    @GetMapping("/{proposalId}/consents")
    public ResponseEntity<ConsenterListResponse> getConsenters(
            @PathVariable Long proposalId,
            @RequestHeader("X-USER-ID") Long userId
    ) {
        // Service 시그니처: (userId, proposalId)
        ConsenterListResponse response = proposalService.getConsenters(userId, proposalId);
        return ResponseEntity.ok(response);
    }

    // =================================================================
    // 2. 편집 프로세스 (Edit Lifecycle)
    // =================================================================

    /**
     * [단계 1] 제안서 생성
     * - 생성 즉시 작성자(A)에게 락 부여 (DRAFTING)
     */
    @PostMapping
    public ResponseEntity<ProposalResponse> createProposal(
            @RequestBody @Valid CreateProposalRequest request,
            @RequestHeader("X-USER-ID") Long userId
    ) {
        // Service 시그니처: (userId, request)
        ProposalResponse response = proposalService.createProposal(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * [단계 3] 편집 모드 진입 (락 획득)
     * - 비관적 락을 사용하여 동시 진입 차단
     * - 성공 시: 상태 DRAFTING 전환, 수정자 갱신, 락 획득
     * - 실패 시: 409 Conflict (PROPOSAL_BEING_EDITED) 발생
     */
    @PostMapping("/{proposalId}/edit/start")
    public ResponseEntity<ProposalResponse> startEditing(
            @PathVariable Long proposalId,
            @RequestHeader("X-USER-ID") Long userId
    ) {
        // Service 시그니처: (userId, proposalId)
        ProposalResponse response = proposalService.startEditing(userId, proposalId);
        return ResponseEntity.ok(response);
    }

    /**
     * [단계 2/3] 내용 수정 (중간 저장)
     * - 락 소유자만 호출 가능
     * - 락이 없거나 만료된 경우 403 Forbidden (NOT_LOCK_OWNER) 발생
     */
    @PutMapping("/{proposalId}")
    public ResponseEntity<ProposalResponse> updateProposal(
            @PathVariable Long proposalId,
            @RequestBody @Valid UpdateProposalRequest request,
            @RequestHeader("X-USER-ID") Long userId
    ) {
        // Service 시그니처: (userId, proposalId, request)
        // 메서드명 변경 반영: updateProposalContent
        ProposalResponse response = proposalService.updateProposalContent(userId, proposalId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * [단계 2] 편집 종료 (임시 저장 및 나가기)
     * - 락을 반납하고 상태를 SAVING으로 변경
     */
    @PostMapping("/{proposalId}/edit/finish")
    public ResponseEntity<Void> finishEditing(
            @PathVariable Long proposalId,
            @RequestHeader("X-USER-ID") Long userId
    ) {
        // Service 시그니처: (userId, proposalId)
        // 메서드명 변경 반영: stopEditing
        proposalService.stopEditing(userId, proposalId);
        return ResponseEntity.ok().build();
    }

    // =================================================================
    // 3. 투표 프로세스 (Voting)
    // =================================================================

    /**
     * [단계 4] 작성 완료 (확정)
     * - 더 이상 수정할 수 없도록 상태를 COMPLETED로 변경
     * - 락 영구 해제
     */
    @PostMapping("/{proposalId}/complete")
    public ResponseEntity<Void> completeProposal(
            @PathVariable Long proposalId,
            @RequestHeader("X-USER-ID") Long userId
    ) {
        // Service 시그니처: (userId, proposalId)
        proposalService.completeProposal(userId, proposalId);
        return ResponseEntity.ok().build();
    }

    /**
     * [단계 4] 투표 시작 설정
     * - 투표 조건(최소 인원, 마감 기한) 설정 및 VOTING 상태 전환
     */
    @PostMapping("/{proposalId}/vote/start")
    public ResponseEntity<ProposalResponse> startVoting(
            @PathVariable Long proposalId,
            @RequestBody @Valid StartVotingRequest request,
            @RequestHeader("X-USER-ID") Long userId
    ) {
        // Service 시그니처: (userId, proposalId, request)
        ProposalResponse response = proposalService.startVoting(userId, proposalId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * [단계 5] 투표하기 (동의)
     * - 중복 투표 체크 후 동의 반영
     * - 조건 달성 시 자동으로 제출 대기(READY_TO_SUBMIT) 상태로 전환됨
     */
    @PostMapping("/{proposalId}/vote")
    public ResponseEntity<Void> consentProposal(
            @PathVariable Long proposalId,
            @RequestHeader("X-USER-ID") Long userId
    ) {
        // Service 시그니처: (userId, proposalId)
        proposalService.consentProposal(userId, proposalId);
        return ResponseEntity.ok().build();
    }
}