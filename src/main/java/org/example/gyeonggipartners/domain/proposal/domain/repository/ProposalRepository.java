package org.example.gyeonggipartners.domain.proposal.domain.repository;

import org.example.gyeonggipartners.domain.proposal.domain.model.Proposal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProposalRepository {


    /** 제안서 저장 (생성 및 수정) */
    Proposal save(Proposal proposal);

    /**
     * ID로 제안서 단건 조회 (락 없음)
     * - 일반적인 조회용
     */
    Optional<Proposal> findById(Long id);

    // --- [기존 정의된 메서드] ---

    /**
     * ID로 제안서 조회 (비관적 락)
     * - SELECT FOR UPDATE
     */
    Optional<Proposal> findByIdWithLock(Long id);

    /**
     * 논의방 내 모든 제안서 조회 (최대 5개)
     */
    List<Proposal> findByRoomId(Long roomId);

    /**
     * 논의방 내 제안서 개수 (5개 제한 검증용)
     */
    int countByRoomId(Long roomId);

    /** 투표 기간이 만료된 VOTING 상태 제안서 조회 (스케줄러용) */
    List<Proposal> findVotingProposalsWithExpiredDeadline(LocalDateTime now);

    /**
     * 제안서 삭제 (Soft Delete)
     */
    void delete(Long id);
}
