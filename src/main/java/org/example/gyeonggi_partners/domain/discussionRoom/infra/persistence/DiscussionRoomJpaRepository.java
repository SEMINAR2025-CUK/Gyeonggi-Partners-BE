package org.example.gyeonggi_partners.domain.discussionRoom.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DiscussionRoom JPA Repository
 * Spring Data JPA 인터페이스
 */
public interface DiscussionRoomJpaRepository extends JpaRepository<DiscussionRoomEntity, Long> {

}
