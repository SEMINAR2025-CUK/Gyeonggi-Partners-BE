package org.example.gyeonggi_partners.domain.discussionRoom.infra.persistence.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Member JPA Repository
 * Spring Data JPA 인터페이스
 */
public interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {

    /**
     * 사용자와 논의방으로 중복 참여 확인
     */
    boolean existsByUserIdAndRoomId(Long userId, Long roomId);

    /**
     * 멤버 삭제 (논의방 나가기)
     */
    void deleteByUserIdAndRoomId(Long userId, Long roomId);

    /**
     * 논의방의 남은 멤버 수 조회
     */
    int countByRoomId(Long roomId);

    /**
     * 사용자가 참여한 논의방 ID 목록 조회 (최신 참여순)
     */
    @Query("SELECT m.roomId FROM MemberEntity m WHERE m.userId = :userId ORDER BY m.createdAt DESC")
    Page<Long> findRoomIdsByUserIdOrderByJoinedAtDesc(Long userId, Pageable pageable);
}
