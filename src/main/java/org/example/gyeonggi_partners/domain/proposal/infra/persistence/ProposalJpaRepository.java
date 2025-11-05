package org.example.gyeonggi_partners.domain.proposal.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ProposalJpaRepository extends JpaRepository<ProposalEntity, Long> {
}
