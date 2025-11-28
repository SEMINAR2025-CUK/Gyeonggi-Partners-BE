package org.example.gyeonggipartners.domain.proposal.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProposalConsentJpaRepository extends JpaRepository<ProposalConsentEntity, Long> {

    boolean existsByProposalIdAndUserId(Long proposalId, Long userId);

    List<ProposalConsentEntity> findByProposalId(Long proposalId);
}