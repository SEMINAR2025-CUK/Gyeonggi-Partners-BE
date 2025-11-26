package org.example.gyeonggipartners.domain.proposal.infra.persistence;

import jakarta.persistence.*;
import lombok.*;
import org.example.gyeonggipartners.domain.common.BaseEntity;
import org.example.gyeonggipartners.domain.discussionroom.infra.persistence.discussionroom.DiscussionRoomEntity;
import org.example.gyeonggipartners.domain.proposal.domain.model.Evidence;
import org.example.gyeonggipartners.domain.proposal.domain.model.Proposal;
import org.example.gyeonggipartners.domain.proposal.domain.model.ProposalStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proposals")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ProposalEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proposal_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private DiscussionRoomEntity room;

    @Column(name = "last_modifier_id")
    private Long lastModifierId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "problem_overview", nullable = false, columnDefinition = "TEXT")
    private String problemOverview;

    @Column(name = "solution", nullable = false, columnDefinition = "TEXT")
    private String solution;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evidences", columnDefinition = "jsonb")
    @Builder.Default
    private List<Evidence> evidences = new ArrayList<>();

    @Column(name = "required_consents", nullable = false)
    private Integer requiredConsents;

    @Column(name = "consent_deadline")
    private LocalDateTime consentDeadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalStatus status;

    @Column(name = "locked_by")
    private Long lockedBy;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    public static ProposalEntity fromDomain(Proposal proposal) {
        return ProposalEntity.builder()
                .id(proposal.getId())
                .room(DiscussionRoomEntity.builder().id(proposal.getRoomId()).build())
                .lastModifierId(proposal.getLastModifierId())
                .title(proposal.getTitle())
                .problemOverview(proposal.getProblemOverview())
                .solution(proposal.getSolution())
                .evidences(proposal.getEvidences())
                .requiredConsents(proposal.getRequiredConsents() != null ? proposal.getRequiredConsents() : 1)
                .consentDeadline(proposal.getConsentDeadline())
                .status(proposal.getStatus())
                .lockedBy(proposal.getLockedBy())
                .lockedAt(proposal.getLockedAt())
                .build();
    }

    public Proposal toDomain() {
        return Proposal.restore(
                this.id,
                this.room.getId(),
                this.lastModifierId,
                this.title,
                this.problemOverview,
                this.solution,
                this.evidences,
                this.requiredConsents,
                this.consentDeadline,
                this.status,
                this.lockedBy,
                this.lockedAt,
                this.getCreatedAt(),
                this.getUpdatedAt(),
                this.getDeletedAt()
        );
    }
}