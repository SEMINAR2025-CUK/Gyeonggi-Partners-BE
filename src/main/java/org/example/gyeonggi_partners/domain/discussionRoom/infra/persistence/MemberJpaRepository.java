package org.example.gyeonggi_partners.domain.discussionRoom.infra.persistence;

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
}
