package org.example.gyeonggi_partners.domain.discussionRoom.infra.persistence.discussionRoom;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.gyeonggi_partners.domain.common.BaseEntity;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.model.AccessLevel;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.model.DiscussionRoom;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.model.Region;
import org.example.gyeonggi_partners.domain.discussionRoom.infra.persistence.member.MemberEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * DiscussionRoom JPA 엔티티
 * DB와 매핑되는 영속성 객체
 */
@Entity
@Table(name = "discussion_rooms")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class DiscussionRoomEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "region", nullable = false)
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false)
    private AccessLevel accessLevel;

    @Column(name = "member_count", nullable = false)
    // DB에 DEFAULT 1이 설정되어 있지만, Entity에도 초기값 1을 명시하는 것이 안전합니다.
    private Integer memberCount = 1;

    @OneToMany(mappedBy = "discussionRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberEntity> members = new ArrayList<>();

    @Builder
    private DiscussionRoomEntity(Long id, String title, String description,
                                 Region region, AccessLevel accessLevel,
                                 Integer memberCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.region = region;
        this.accessLevel = accessLevel;
        this.memberCount = memberCount;
    }

    /**
     * 도메인 모델을 엔티티로 변환 (Domain -> Entity)
     */
    public static DiscussionRoomEntity fromDomain(DiscussionRoom discussionRoom) {
        return DiscussionRoomEntity.builder()
                .id(discussionRoom.getId())
                .title(discussionRoom.getTitle())
                .description(discussionRoom.getDescription())
                .region(discussionRoom.getRegion())
                .accessLevel(discussionRoom.getAccessLevel())
                .memberCount(discussionRoom.getMemberCount())
                .build();
    }

    /**
     * 엔티티를 도메인 모델로 변환 (Entity -> Domain)
     */
    public DiscussionRoom toDomain() {
        return DiscussionRoom.restore(
                this.id,
                this.title,
                this.description,
                this.region,
                this.accessLevel,
                this.memberCount,
                this.getCreatedAt(),
                this.getUpdatedAt(),
                this.getDeletedAt()
        );
    }
}
