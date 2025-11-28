package org.example.gyeonggipartners.domain.proposal.infra.persistence;

import lombok.RequiredArgsConstructor;
import org.example.gyeonggipartners.domain.proposal.domain.model.ProposalConsent;
import org.example.gyeonggipartners.domain.proposal.domain.repository.ProposalConsentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProposalConsentRepositoryImpl implements ProposalConsentRepository {

    private final ProposalConsentJpaRepository proposalConsentJpaRepository;

    @Override
    public void save(ProposalConsent consent) {
        proposalConsentJpaRepository.save(ProposalConsentEntity.fromDomain(consent));
    }

    @Override
    public boolean existsByProposalIdAndUserId(Long proposalId, Long userId) {
        return proposalConsentJpaRepository.existsByProposalIdAndUserId(proposalId, userId);
    }

    @Override
    public List<ProposalConsent> findByProposalId(Long proposalId) {
        return proposalConsentJpaRepository.findByProposalId(proposalId).stream()
                .map(ProposalConsentEntity::toDomain)
                .toList();
    }
}