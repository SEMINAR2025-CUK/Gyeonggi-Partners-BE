package org.example.gyeonggipartners.domain.proposal.domain.repository;


import org.example.gyeonggipartners.domain.proposal.domain.model.ProposalConsent;
import java.util.List;

/**
 * ProposalConsent Repository 인터페이스
 * 제안서 동의 관리 (복합키 구조)
 */
public interface ProposalConsentRepository {

    /**
     * 동의 추가
     */
    void save(ProposalConsent consent);

    /**
     * 동의 취소 (복합키로 삭제)
     */
    void deleteByProposalIdAndUserId(Long proposalId, Long userId);

    /**
     * 중복 동의 체크
     */
    boolean existsByProposalIdAndUserId(Long proposalId, Long userId);

    /**
     * 제안서의 모든 동의자 조회 (닉네임 표시 & 동의자 수 조회)
     */
    List<ProposalConsent> findByProposalId(Long proposalId);
}
