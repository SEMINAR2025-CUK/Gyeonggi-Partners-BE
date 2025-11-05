package org.example.gyeonggi_partners.domain.proposal.infra.persistence;

import jakarta.persistence.*;
import lombok.*;
import org.example.gyeonggi_partners.domain.common.BaseEntity;
import org.example.gyeonggi_partners.domain.proposal.domain.model.Consenter;
import org.example.gyeonggi_partners.domain.proposal.domain.model.ContentFormat;
import org.example.gyeonggi_partners.domain.proposal.domain.model.Proposal;
import org.example.gyeonggi_partners.domain.proposal.domain.model.SubmitStatus;
import org.example.gyeonggi_partners.domain.proposal.infra.mock.RoomEntity;
import org.example.gyeonggi_partners.domain.user.infra.persistence.UserEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ProposalEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private RoomEntity room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity author;

    // 제안서 - 제목, 내용
    private String title;

    @Enumerated(EnumType.STRING)
    private SubmitStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "consents", columnDefinition = "jsonb")
    private List<Consenter> consents;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contents", columnDefinition = "jsonb")
    private ContentFormat contents;
    private LocalDateTime deadline;


    public static ProposalEntity fromDomain(Proposal proposal) {
        ProposalEntity entity = ProposalEntity.builder()
                .id(proposal.getId())
                .room(new RoomEntity(proposal.getRoomId()))
                .author(new UserEntity(proposal.getAuthorId()))
                .title(proposal.getTitle())
                .status(proposal.getStatus())
                .consents(proposal.getConsents())
                .contents(proposal.getContents())
                .deadline(proposal.getDeadline())
                .build();

        return entity;
    }

    public Proposal toDomain() {
        return Proposal.restore(
                this.id,
                this.room.getId(),
                this.author.getId(),
                this.title,
                this.contents,
                this.consents,
                this.status,
                this.deadline,
                this.getCreatedAt(),
                this.getUpdatedAt(),
                this.getDeletedAt()
        );
    }



}
