package org.example.gyeonggipartners.domain.proposal.infra.persistence;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.gyeonggipartners.domain.discussionroom.infra.persistence.discussionroom.DiscussionRoomEntity;
import org.example.gyeonggipartners.domain.proposal.domain.model.Proposal;
import org.example.gyeonggipartners.domain.proposal.domain.repository.ProposalRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProposalRepositoryImpl implements ProposalRepository {

    private final ProposalJpaRepository proposalJpaRepository;
    private final EntityManager entityManager; // [추가] 프록시 객체 조회를 위해 필요

    @Override
    public Proposal save(Proposal proposal) {
        ProposalEntity entity = ProposalEntity.fromDomain(proposal);

        // DB 조회 없이 프록시로 연관관계 설정 (FK만 필요하므로)
        DiscussionRoomEntity roomRef = entityManager.getReference(DiscussionRoomEntity.class, proposal.getRoomId());
        entity.setRoom(roomRef);

        ProposalEntity savedEntity = proposalJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Proposal> findById(Long id) {
        return proposalJpaRepository.findById(id)
                .map(ProposalEntity::toDomain);
    }

    @Override
    public Optional<Proposal> findByIdWithLock(Long id) {
        return proposalJpaRepository.findByIdWithLock(id)
                .map(ProposalEntity::toDomain);
    }

    @Override
    public List<Proposal> findByRoomId(Long roomId) {
        return proposalJpaRepository.findByRoomId(roomId).stream()
                .map(ProposalEntity::toDomain)
                .toList();
    }

    @Override
    public int countByRoomId(Long roomId) {
        return proposalJpaRepository.countByRoomId(roomId);
    }

    /**
     * 투표 마감 기한이 지난 제안서 조회 (스케줄러 사용)
     * 참고: ProposalRepository 인터페이스에 해당 메서드 정의가 필요할 수 있습니다.
     */
    public List<Proposal> findVotingProposalsWithExpiredDeadline(LocalDateTime now) {
        return proposalJpaRepository.findVotingProposalsWithExpiredDeadline(now).stream()
                .map(ProposalEntity::toDomain)
                .toList();
    }

    @Override
    public void delete(Long id) {
        proposalJpaRepository.deleteById(id);
    }
}