package org.example.gyeonggi_partners.domain.discussionRoom.domain.repository;

import org.example.gyeonggi_partners.domain.discussionRoom.domain.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Member 도메인 Repository 인터페이스
 * 도메인 계층에서 정의하고, 인프라 계층에서 구현
 */
public interface MemberRepository {

    /**
     * 멤버 저장 (논의방 참여)
     * @param member 저장할 멤버
     * @return 저장된 멤버 (ID 포함)
     */
    Member save(Member member);

    /**
     * 중복 참여 확인
     * @param userId 사용자 ID
     * @param roomId 논의방 ID
     * @return 이미 참여 중이면 true
     */
    boolean existsByUserIdAndRoomId(Long userId, Long roomId);

    /**
     * 멤버 삭제 (논의방 나가기)
     * @param userId 사용자 ID
     * @param roomId 논의방 ID
     */
    void deleteByUserIdAndRoomId(Long userId, Long roomId);

    /**
     * 논의방의 남은 멤버 수 조회
     * @param roomId 논의방 ID
     * @return 남은 멤버 수
     */
    int countByRoomId(Long roomId);

    /**
     * 사용자가 참여한 논의방 ID 목록 조회 (페이징, 최신 참여순)
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 논의방 ID 목록
     */
    Page<Long> findRoomIdsByUserId(Long userId, Pageable pageable);
}
