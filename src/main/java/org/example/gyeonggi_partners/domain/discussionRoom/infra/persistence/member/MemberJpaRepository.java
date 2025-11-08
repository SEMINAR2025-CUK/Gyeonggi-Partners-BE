package org.example.gyeonggi_partners.domain.discussionRoom.infra.persistence.member;

import org.springframework.data.jpa.repository.JpaRepository;

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
}
