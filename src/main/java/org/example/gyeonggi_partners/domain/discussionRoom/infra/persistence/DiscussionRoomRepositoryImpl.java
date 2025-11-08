package org.example.gyeonggi_partners.domain.discussionRoom.infra.persistence;

import lombok.RequiredArgsConstructor;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.model.DiscussionRoom;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.repository.DiscussionRoomRepository;
import org.springframework.stereotype.Repository;

/**
 * DiscussionRoomRepository 구현체
 * 도메인 인터페이스를 JPA로 구현
 */
@Repository
@RequiredArgsConstructor
public class DiscussionRoomRepositoryImpl implements DiscussionRoomRepository {

    private final DiscussionRoomJpaRepository discussionRoomJpaRepository;

    @Override
    public DiscussionRoom save(DiscussionRoom discussionRoom) {
        DiscussionRoomEntity entity = DiscussionRoomEntity.fromDomain(discussionRoom);
        DiscussionRoomEntity savedEntity = discussionRoomJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public DiscussionRoom findById(Long id) {
        return discussionRoomJpaRepository.findById(id)
                .map(DiscussionRoomEntity::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("논의방을 찾을 수 없습니다. ID: " + id));
    }

    @Override
    public void softDelete(Long roomId) {
        discussionRoomJpaRepository.softDelete(roomId);
    }
}
