package org.example.gyeonggi_partners.domain.discussionRoom.infra.persistence.discussionRoom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * DiscussionRoom JPA Repository
 * Spring Data JPA 인터페이스
 */
public interface DiscussionRoomJpaRepository extends JpaRepository<DiscussionRoomEntity, Long> {

    /**
     * Soft Delete: deletedAt을 현재 시각으로 업데이트
     * 실제 데이터는 삭제하지 않고 삭제 표시만 함 (범죄 수사 대응)
     */
    @Modifying
    @Query("UPDATE DiscussionRoomEntity d SET d.deletedAt = CURRENT_TIMESTAMP WHERE d.id = :id")
    void softDelete(@Param("id") Long id);

    Optional<DiscussionRoomEntity> findByDiscussionRoomId(Long discussionRoomId);
}
