package org.example.gyeonggipartners.domain.proposal.application;

import lombok.RequiredArgsConstructor;
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
import org.example.gyeonggipartners.domain.user.exception.UserErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProposalService {

    private static final int MAX_PROPOSAL_LIMIT = 5;

    private final ProposalRepository proposalRepository;
    private final ProposalConsentRepository proposalConsentRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    // =================================================================
    // 1. [생성] Create
    // =================================================================

    /**
     * 제안서 생성
     */
    public ProposalResponse createProposal(CreateProposalRequest request, Long userId) {
        // 1. 권한 및 규칙 검증
        validateRoomMember(request.getRoomId(), userId);
        validateProposalCountLimit(request.getRoomId());

        // 2. 도메인 객체 생성
        Proposal proposal = Proposal.create(
                request.getRoomId(),
                userId,
                request.getTitle(),
                request.getProblemOverview(),
                request.getSolution(),
                request.toEvidenceDomains()
        );

        // 3. 저장 및 반환
        return ProposalResponse.from(proposalRepository.save(proposal));
    }

    // =================================================================
    // 2. [편집] Editing (Lock & Update)
    // =================================================================

    /**
     * 편집 모드 진입 (비관적 락 획득)
     */
    public ProposalResponse startEditing(Long proposalId, Long userId) {
        // 1. 락을 걸고 조회 (동시성 제어)
        Proposal proposal = getProposalWithLock(proposalId);
        validateRoomMember(proposal.getRoomId(), userId);

        // 2. 도메인 로직 수행 (상태 변경, 수정자 갱신)
        try {
            proposal.startEditing(userId);
        } catch (IllegalStateException e) {
            throw new BusinessException(ProposalErrorCode.PROPOSAL_BEING_EDITED);
        }

        // 3. 변경 사항 반영 (Dirty Checking) 및 반환
        return ProposalResponse.from(proposal);
    }

    /**
     * 내용 수정 (중간 저장)
     */
    public ProposalResponse updateProposal(Long proposalId, UpdateProposalRequest request, Long userId) {
        Proposal proposal = getProposalOrThrow(proposalId);
        validateRoomMember(proposal.getRoomId(), userId);

        // 1. 도메인 로직 위임 (락 소유자 검증 포함)
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
     * 편집 종료 (나가기)
     */
    public void finishEditing(Long proposalId, Long userId) {
        Proposal proposal = getProposalOrThrow(proposalId);
        validateRoomMember(proposal.getRoomId(), userId);

        // 락 해제 시도 (내 락이 아니면 무시됨)
        try {
            proposal.saveAsDraft(userId);
        } catch (IllegalStateException e) {
            // 이미 락을 뺏겼거나 만료된 경우 등은 조용히 넘어감
        }
    }

    /**
     * 작성 완료 (확정)
     */
    public void completeProposal(Long proposalId, Long userId) {
        Proposal proposal = getProposalOrThrow(proposalId);
        validateRoomMember(proposal.getRoomId(), userId);

        try {
            proposal.complete(userId);
        } catch (IllegalStateException e) {
            throw new BusinessException(ProposalErrorCode.NOT_LOCK_OWNER);
        }
    }

    // =================================================================
    // 3. [투표] Voting & Consent
    // =================================================================

    /**
     * 투표 시작 (설정)
     */
    public ProposalResponse startVoting(Long proposalId, StartVotingRequest request, Long userId) {
        Proposal proposal = getProposalOrThrow(proposalId);
        validateRoomMember(proposal.getRoomId(), userId);

        try {
            proposal.startVoting(request.getRequiredConsents(), request.getConsentDeadline());
        } catch (IllegalStateException e) {
            throw new BusinessException(ProposalErrorCode.ALREADY_VOTING);
        }

        return ProposalResponse.from(proposal);
    }

    /**
     * 제안서 동의 (투표하기)
     */
    public void consentProposal(Long proposalId, Long userId) {
        Proposal proposal = getProposalOrThrow(proposalId);
        validateRoomMember(proposal.getRoomId(), userId);

        // 1. 투표 가능 상태 검증
        validateVotingStatus(proposal);

        // 2. 중복 투표 검증
        if (proposalConsentRepository.existsByProposalIdAndUserId(proposalId, userId)) {
            throw new BusinessException(ProposalErrorCode.ALREADY_CONSENTED);
        }

        // 3. 동의 저장
        proposalConsentRepository.save(ProposalConsent.create(proposalId, userId));

        // 4. 투표 종료 조건 체크 (인원 충족 시 즉시 종료)
        checkAndFinishVotingIfQualified(proposal);
    }

    /**
     * 동의자 목록 조회
     */
    @Transactional(readOnly = true)
    public ConsenterListResponse getConsenters(Long proposalId, Long userId) {
        Proposal proposal = getProposalOrThrow(proposalId);
        validateRoomMember(proposal.getRoomId(), userId);

        // 1. 동의 내역 조회
        List<ProposalConsent> consents = proposalConsentRepository.findByProposalId(proposalId);
        if (consents.isEmpty()) {
            return ConsenterListResponse.of(List.of());
        }

        // 2. 사용자 ID 목록 추출
        List<Long> userIds = consents.stream()
                .map(ProposalConsent::getUserId)
                .toList();

        // 3. 사용자 정보(닉네임) 조회 및 매핑
        Map<Long, String> nicknameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        // 4. DTO 변환
        List<ConsenterDto> consenterDtos = consents.stream()
                .map(c -> new ConsenterDto(c.getUserId(), nicknameMap.getOrDefault(c.getUserId(), "알 수 없음")))
                .toList();

        return ConsenterListResponse.of(consenterDtos);
    }

    // =================================================================
    // 4. [조회] Read
    // =================================================================

    @Transactional(readOnly = true)
    public ProposalResponse getProposal(Long proposalId, Long userId) {
        Proposal proposal = getProposalOrThrow(proposalId);
        validateRoomMember(proposal.getRoomId(), userId);
        return ProposalResponse.from(proposal);
    }

    @Transactional(readOnly = true)
    public List<ProposalResponse> getProposalsByRoom(Long roomId, Long userId) {
        validateRoomMember(roomId, userId);

        return proposalRepository.findByRoomId(roomId).stream()
                .map(ProposalResponse::from)
                .toList();
    }

    // =================================================================
    // 5. [헬퍼 메서드] Private Helpers
    // =================================================================

    private Proposal getProposalOrThrow(Long proposalId) {
        return proposalRepository.findById(proposalId)
                .orElseThrow(() -> new BusinessException(ProposalErrorCode.PROPOSAL_NOT_FOUND));
    }

    private Proposal getProposalWithLock(Long proposalId) {
        return proposalRepository.findByIdWithLock(proposalId)
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

    private void validateVotingStatus(Proposal proposal) {
        // 도메인 모델에 isVoting() 메서드가 있다면 그것을 사용해도 좋음
        if (proposal.getStatus() != org.example.gyeonggipartners.domain.proposal.domain.model.ProposalStatus.VOTING) {
            throw new BusinessException(ProposalErrorCode.NOT_IN_VOTING);
        }
    }

    private void checkAndFinishVotingIfQualified(Proposal proposal) {
        List<ProposalConsent> currentConsents = proposalConsentRepository.findByProposalId(proposal.getId());
        try {
            // 도메인 로직에게 종료 판단 위임
            proposal.finishVoting(currentConsents.size());
        } catch (IllegalStateException e) {
            // 아직 조건이 충족되지 않았으면 무시 (VOTING 상태 유지)
        }
    }
}