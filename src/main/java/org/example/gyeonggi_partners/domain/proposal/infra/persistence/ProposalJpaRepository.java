package org.example.gyeonggi_partners.domain.proposal.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

public interface ProposalJpaRepository extends JpaRepository<ProposalEntity, Long> {

    @Query("SELECT p FROM ProposalEntity p WHERE p.status = 'VOTING' AND p.deadline < :now")
    List<ProposalEntity> findVotingProposalsWithExpiredDeadline(@Param("now") LocalDateTime now);


    @Query("SELECT COUNT(m) > 0 FROM DiscussionRoomEntity r JOIN r.members m " +
            "WHERE r.id = :roomId AND m.id = :userId")
    boolean existsMemberInRoom(@Param("userId") Long userId,@Param("roomId") Long roomId);
}
