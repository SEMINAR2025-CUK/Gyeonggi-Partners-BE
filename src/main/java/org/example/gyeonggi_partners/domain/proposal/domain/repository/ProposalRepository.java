package org.example.gyeonggi_partners.domain.proposal.domain.repository;

import org.example.gyeonggi_partners.domain.proposal.domain.model.Proposal;

import java.util.Optional;

public interface ProposalRepository {

    Proposal save(Proposal proposal);

    Optional<Proposal> findById(Long proposalId);

}
