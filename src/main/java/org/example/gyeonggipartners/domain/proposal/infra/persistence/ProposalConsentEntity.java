package org.example.gyeonggipartners.domain.proposal.infra.persistence;

import jakarta.persistence.*;
import lombok.*;
import org.example.gyeonggipartners.domain.common.BaseEntity;
import org.example.gyeonggipartners.domain.proposal.domain.model.ProposalConsent;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "proposal_consents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_proposal_consent_proposal_user",
                        columnNames = {"proposal_id", "user_id"}
                )
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ProposalConsentEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consent_id")
    private Long id;

    @Column(name = "proposal_id", nullable = false)
    private Long proposalId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ProposalConsentEntity fromDomain(ProposalConsent domain) {
        return ProposalConsentEntity.builder()
                .proposalId(domain.getProposalId())
                .userId(domain.getUserId())
                .createdAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    public ProposalConsent toDomain() {
        return ProposalConsent.restore(
                this.proposalId,
                this.userId,
                this.createdAt
        );
    }
}