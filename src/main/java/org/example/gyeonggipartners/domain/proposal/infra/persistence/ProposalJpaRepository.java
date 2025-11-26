package org.example.gyeonggipartners.domain.proposal.infra.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProposalJpaRepository extends JpaRepository<ProposalEntity, Long> {

    /**
     * 비관적 락을 사용한 단건 조회 (편집 진입 시 동시성 제어)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProposalEntity p WHERE p.id = :id")
    Optional<ProposalEntity> findByIdWithLock(@Param("id") Long id);

    /**
     * 투표 기간이 만료된 제안서 조회 (스케줄러용)
     */
    @Query("SELECT p FROM ProposalEntity p WHERE p.status = 'VOTING' AND p.consentDeadline < :now")
    List<ProposalEntity> findVotingProposalsWithExpiredDeadline(@Param("now") LocalDateTime now);

    /**
     * 특정 논의방의 제안서 목록 조회 (최신순)
     */
    @Query("SELECT p FROM ProposalEntity p WHERE p.room.id = :roomId ORDER BY p.createdAt DESC")
    List<ProposalEntity> findByRoomId(@Param("roomId") Long roomId);

    /**
     * 논의방 내 제안서 개수 카운트
     */
    int countByRoomId(Long roomId);
}