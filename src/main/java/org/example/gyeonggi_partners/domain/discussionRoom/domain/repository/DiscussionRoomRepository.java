package org.example.gyeonggi_partners.domain.discussionRoom.domain.repository;

import org.example.gyeonggi_partners.domain.discussionRoom.domain.model.DiscussionRoom;

/**
 * DiscussionRoom 도메인 Repository 인터페이스
 * 도메인 계층에서 정의하고, 인프라 계층에서 구현
 */
public interface DiscussionRoomRepository {

    /**
     * 논의방 저장 (생성)
     * @param discussionRoom 저장할 논의방
     * @return 저장된 논의방 (ID 포함)
     */
    DiscussionRoom save(DiscussionRoom discussionRoom);

    /**
     * 논의방 ID로 조회
     * @param id 논의방 ID
     * @return 조회된 논의방 (응답 DTO 생성용)
     */
    DiscussionRoom findById(Long id);
}
