package org.example.gyeonggi_partners.domain.discussionRoom.infra.persistence;

import lombok.RequiredArgsConstructor;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.model.Member;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.repository.MemberRepository;
import org.springframework.stereotype.Repository;

/**
 * MemberRepository 구현체
 * 도메인 인터페이스를 JPA로 구현
 */
@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Member save(Member member) {
        MemberEntity entity = MemberEntity.fromDomain(member);
        MemberEntity savedEntity = memberJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public boolean existsByUserIdAndRoomId(Long userId, Long roomId) {
        return memberJpaRepository.existsByUserIdAndRoomId(userId, roomId);
    }

    @Override
    public void deleteByUserIdAndRoomId(Long userId, Long roomId) {
        memberJpaRepository.deleteByUserIdAndRoomId(userId, roomId);
    }

    @Override
    public int countByRoomId(Long roomId) {
        return memberJpaRepository.countByRoomId(roomId);
    }
}
