package org.example.gyeonggi_partners.domain.discussionRoom.domain.repository;

import org.example.gyeonggi_partners.domain.discussionRoom.domain.model.Member;

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
}
