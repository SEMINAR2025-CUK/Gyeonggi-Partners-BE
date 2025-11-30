package org.example.gyeonggipartners.domain.proposal.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.gyeonggipartners.common.exception.BusinessException;
import org.example.gyeonggipartners.domain.discussionroom.domain.repository.MemberRepository;
import org.example.gyeonggipartners.domain.proposal.api.dto.*;
import org.example.gyeonggipartners.domain.proposal.domain.model.Proposal;
import org.example.gyeonggipartners.domain.proposal.domain.model.ProposalConsent;
import org.example.gyeonggipartners.domain.proposal.domain.repository.ProposalConsentRepository;
import org.example.gyeonggipartners.domain.proposal.domain.repository.ProposalRepository;
import org.example.gyeonggipartners.domain.proposal.exception.ProposalErrorCode;
import org.example.gyeonggipartners.domain.user.domain.model.User;
import org.example.gyeonggipartners.domain.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProposalService {

    private static final int MAX_PROPOSAL_LIMIT = 5;

    private final ProposalRepository proposalRepository;
    private final ProposalConsentRepository proposalConsentRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    // =================================================================
    // 단계 1. 제안서 생성 및 독점 작성 (사용자 A)
    // =================================================================

    /**
     * 제안서 생성
     * - 생성 즉시 DRAFTING 상태가 되며, 작성자(A)가 락을 소유함
     */
    public ProposalResponse createProposal(Long userId, CreateProposalRequest request) {
        validateRoomMember(request.getRoomId(), userId);
        validateProposalCountLimit(request.getRoomId());

        Proposal proposal = Proposal.create(
                request.getRoomId(),
                userId,
                request.getTitle(),
                request.getProblemOverview(),
                request.getSolution(),
                request.toEvidenceDomains()
        );

        return ProposalResponse.from(proposalRepository.save(proposal));
    }

    // =================================================================
    // 단계 2. 임시 저장 및 권한 반납 (A -> 공용)
    // =================================================================

    /**
     * 내용 수정 (중간 저장)
     * - 락을 소유한 사용자만 호출 가능
     * - 상태 변경 없이 내용만 업데이트함
     */
    public ProposalResponse updateProposalContent(Long userId, Long proposalId, UpdateProposalRequest request) {
        Proposal proposal = getProposalOrThrow(proposalId);
        validateRoomMember(proposal.getRoomId(), userId);

        try {
            proposal.updateContent(
                    userId,
                    request.getTitle(),
                    request.getProblemOverview(),
                    request.getSolution(),
                    request.toEvidenceDomains()
            );
        } catch (IllegalStateException e) {
            throw new BusinessException(ProposalErrorCode.NOT_LOCK_OWNER);
        }

        return ProposalResponse.from(proposal);
    }

    /**
     * 편집 종료 (임시 저장 및 나가기)
     * - 상태를 SAVING으로 변경하고 락을 해제(반납)함
     * - 이후 다른 사용자가 접근 가능해짐
     */
    public ProposalResponse stopEditing(Long userId, Long proposalId) {
        Proposal proposal = getProposalOrThrow(proposalId);
        validateRoomMember(proposal.getRoomId(), userId);

        try {
            proposal.saveAsDraft(userId);
        } catch (IllegalStateException e) {
            // '나가기' 행위에서 락 권한이 없으면 명시적 예외 발생이 맞음
            throw new BusinessException(ProposalErrorCode.NOT_LOCK_OWNER);
        }

        return ProposalResponse.from(proposalRepository.save(proposal));
    }

    // =================================================================
    // 단계 3. 릴레이 편집 (사용자 B 진입)
    // =================================================================

    /**
     * 편집 모드 진입 (릴레이 시작)
     * - 비관적 락(DB Lock)을 사용하여 동시 진입을 원천 차단
     * - 상태를 DRAFTING으로 변경하고, 최근 수정자를 요청자(B)로 갱신
     */
    public ProposalResponse startEditing(Long userId, Long proposalId) {
        // SELECT ... FOR UPDATE로 동시성 제어
        Proposal proposal = proposalRepository.findByIdWithLock(proposalId)
                .orElseThrow(() -> new BusinessException(ProposalErrorCode.PROPOSAL_NOT_FOUND));

        validateRoomMember(proposal.getRoomId(), userId);
        validateEditableStatus(proposal);

        try {
            proposal.startEditing(userId);
        } catch (IllegalStateException e) {
            throw new BusinessException(ProposalErrorCode.PROPOSAL_BEING_EDITED);
        }

        return ProposalResponse.from(proposalRepository.save(proposal));
    }

    // =================================================================
    // 단계 4. 작성 완료 및 투표 설정
    // =================================================================

    /**
     * 작성 완료 (확정)
     * - 내용을 더 이상 수정할 수 없도록 COMPLETED 상태로 변경
     * - 락은 해제됨
     */
    public void completeProposal(Long userId, Long proposalId) {
        Proposal proposal = getProposalOrThrow(proposalId);
        validateRoomMember(proposal.getRoomId(), userId);

        try {
            proposal.complete(userId);
        } catch (IllegalStateException e) {
            throw new BusinessException(ProposalErrorCode.NOT_LOCK_OWNER);
        }

        proposalRepository.save(proposal);
    }

    /**
     * 투표 설정 및 시작
     * - 최소 동의 인원과 마감 기한을 설정하고 VOTING 상태로 전환
     */
    public ProposalResponse startVoting(Long userId, Long proposalId, StartVotingRequest request) {
        Proposal proposal = getProposalOrThrow(proposalId);
        validateRoomMember(proposal.getRoomId(), userId);

        try {
            proposal.startVoting(request.getRequiredConsents(), request.getConsentDeadline());
        } catch (IllegalStateException e) {
            throw new BusinessException(ProposalErrorCode.ALREADY_VOTING);
        }

        return ProposalResponse.from(proposalRepository.save(proposal));
    }

    // =================================================================
    // 단계 5. 투표 진행 및 종료
    // =================================================================

    /**
     * 제안서 동의 (투표하기)
     * - 중복 투표 방지 및 동의 내역 저장
     * - 투표 조건 충족 시 제안서 상태를 자동 변경
     */
    public void consentProposal(Long userId, Long proposalId) {
        Proposal proposal = getProposalOrThrow(proposalId);
        validateRoomMember(proposal.getRoomId(), userId);

        if (proposalConsentRepository.existsByProposalIdAndUserId(proposalId, userId)) {
            throw new BusinessException(ProposalErrorCode.ALREADY_CONSENTED);
        }

        ProposalConsent consent = ProposalConsent.create(proposalId, userId);
        proposalConsentRepository.save(consent);

        // 주의: 방금 저장한 동의를 포함하기 위해 다시 조회
        int currentConsentCount = proposalConsentRepository.findByProposalId(proposalId).size();

        try {
            proposal.finishVoting(currentConsentCount);
            proposalRepository.save(proposal);
        } catch (IllegalStateException e) {
            // 조건 미충족 시 예외 발생하나, 정상 흐름이므로 무시
        }
    }

    /**
     * 동의자 목록 조회
     */
    @Transactional(readOnly = true)
    public ConsenterListResponse getConsenters(Long userId, Long proposalId) {
        Proposal proposal = getProposalOrThrow(proposalId);
        validateRoomMember(proposal.getRoomId(), userId);

        List<ProposalConsent> consents = proposalConsentRepository.findByProposalId(proposalId);

        if (consents.isEmpty()) {
            return ConsenterListResponse.of(List.of());
        }

        List<Long> userIds = consents.stream().map(ProposalConsent::getUserId).toList();
        Map<Long, String> nicknameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        List<ConsenterDto> dtos = consents.stream()
                .map(c -> new ConsenterDto(c.getUserId(), nicknameMap.getOrDefault(c.getUserId(), "알 수 없음")))
                .toList();

        return ConsenterListResponse.of(dtos);
    }

    // =================================================================
    // [조회] Read Operations
    // =================================================================

    /**
     * 제안서 단건 조회
     */
    @Transactional(readOnly = true)
    public ProposalResponse getProposal(Long userId, Long proposalId) {
        Proposal proposal = getProposalOrThrow(proposalId);
        validateRoomMember(proposal.getRoomId(), userId);
        return ProposalResponse.from(proposal);
    }

    /**
     * 논의방 내 제안서 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ProposalResponse> getProposalsByRoom(Long userId, Long roomId) {
        validateRoomMember(roomId, userId);
        return proposalRepository.findByRoomId(roomId).stream()
                .map(ProposalResponse::from)
                .toList();
    }

    // =================================================================
    // [헬퍼] Helper Methods
    // =================================================================

    private Proposal getProposalOrThrow(Long proposalId) {
        return proposalRepository.findById(proposalId)
                .orElseThrow(() -> new BusinessException(ProposalErrorCode.PROPOSAL_NOT_FOUND));
    }

    private void validateRoomMember(Long roomId, Long userId) {
        if (!memberRepository.existsByUserIdAndRoomId(userId, roomId)) {
            throw new BusinessException(ProposalErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private void validateProposalCountLimit(Long roomId) {
        if (proposalRepository.countByRoomId(roomId) >= MAX_PROPOSAL_LIMIT) {
            throw new BusinessException(ProposalErrorCode.PROPOSAL_LIMIT_EXCEEDED);
        }
    }

    /**
     * 편집 진입 가능 상태 검증 - SAVING만 허용, 상태별 예외 분기
     */
    private void validateEditableStatus(Proposal proposal) {
        switch (proposal.getStatus()) {
            case DRAFTING:
                throw new BusinessException(ProposalErrorCode.PROPOSAL_BEING_EDITED);
            case VOTING:
                throw new BusinessException(ProposalErrorCode.CANNOT_EDIT_IN_VOTING);
            case COMPLETED:
                throw new BusinessException(ProposalErrorCode.CANNOT_EDIT_COMPLETED);
            case READY_TO_SUBMIT:
                throw new BusinessException(ProposalErrorCode.CANNOT_EDIT_READY_TO_SUBMIT);
            case SUBMITTED:
                throw new BusinessException(ProposalErrorCode.CANNOT_EDIT_SUBMITTED);
            case SAVING:
                break;
            default:
                throw new BusinessException(ProposalErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    /**
     * 투표 기간 만료 제안서 일괄 종료 처리
     * - 스케줄러에 의해 주기적으로 호출됨
     */
    @Transactional
    public int closeExpiredProposals() {
        List<Proposal> expiredProposals = proposalRepository.findVotingProposalsWithExpiredDeadline(LocalDateTime.now());

        int count = 0;
        for (Proposal proposal : expiredProposals) {
            try {
                int currentConsentCount = proposalConsentRepository.findByProposalId(proposal.getId()).size();

                // finishVoting은 "인원 충족 OR 시간 경과"면 통과하므로 시간 만료 시 인원 미달이어도 종료됨
                proposal.finishVoting(currentConsentCount);
                count++;
            } catch (Exception e) {
                log.error("제안서 {} 자동 종료 실패: {}", proposal.getId(), e.getMessage());
            }
        }
        return count;
    }
}
