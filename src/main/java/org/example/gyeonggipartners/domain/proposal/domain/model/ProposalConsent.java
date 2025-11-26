package org.example.gyeonggipartners.domain.proposal.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 제안서 동의 Entity
 * proposal_consents 테이블과 매핑
 * 복합키: (proposalId, userId)
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProposalConsent {

    private Long proposalId;
    private Long userId;
    private LocalDateTime createdAt;

    /**
     * 새로운 동의 생성
     */
    public static ProposalConsent create(Long proposalId, Long userId) {
        if (proposalId == null) {
            throw new IllegalArgumentException("제안서 ID는 필수입니다.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        return new ProposalConsent(
                proposalId,
                userId,
                LocalDateTime.now()
        );
    }

    /**
     * 기존 동의 복원 (DB에서 조회)
     */
    public static ProposalConsent restore(Long proposalId, Long userId, LocalDateTime createdAt) {
        return new ProposalConsent(proposalId, userId, createdAt);
    }
}
