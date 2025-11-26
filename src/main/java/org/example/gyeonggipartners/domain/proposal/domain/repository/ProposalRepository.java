package org.example.gyeonggipartners.domain.proposal.domain.repository;

import org.example.gyeonggipartners.domain.proposal.domain.model.Proposal;
import java.util.List;
import java.util.Optional;

public interface ProposalRepository {

    /**
     * ID로 제안서 조회 (비관적 락)
     * SELECT FOR UPDATE
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

    /**
     * 제안서 삭제 (Soft Delete)
     */
    void delete(Long id);
}
