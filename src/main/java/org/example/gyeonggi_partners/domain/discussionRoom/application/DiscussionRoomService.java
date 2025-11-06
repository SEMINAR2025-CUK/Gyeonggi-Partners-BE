package org.example.gyeonggi_partners.domain.discussionRoom.application;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.gyeonggi_partners.domain.discussionRoom.api.dto.CreateDiscussionRoomReq;
import org.example.gyeonggi_partners.domain.discussionRoom.api.dto.CreateDiscussionRoomRes;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.model.DiscussionRoom;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.model.Member;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.repository.DiscussionRoomRepository;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.repository.MemberRepository;
import org.example.gyeonggi_partners.domain.discussionRoom.infra.cache.DiscussionRoomCacheRepository;
import org.example.gyeonggi_partners.domain.discussionRoom.infra.cache.dto.CachedDiscussionRoom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DiscussionRoomService {

    private final DiscussionRoomRepository discussionRoomRepository;
    private final MemberRepository memberRepository;
    private final DiscussionRoomCacheRepository cacheRepository;

    /**
     * 논의방 생성
     * Write-Through 전략: DB 저장 후 Redis 캐싱
     * 
     * @param request 논의방 생성 요청
     * @param userId 생성자 ID (현재 로그인한 사용자)
     * @return 생성된 논의방 정보
     */
    public CreateDiscussionRoomRes createDiscussionRoom(CreateDiscussionRoomReq request, Long userId) {
        log.info("논의방 생성 요청 - userId: {}, title: {}", userId, request.getTitle());
        
        // 1. Domain 생성 (비즈니스 로직 & 유효성 검증)
        DiscussionRoom room = DiscussionRoom.create(
            request.getTitle(),
            request.getDescription(),
            request.getRegion(),
            request.getAccessLevel()
        );

        // 2. DB 저장
        DiscussionRoom savedRoom = discussionRoomRepository.save(room);
        log.debug("DB 저장 완료 - roomId: {}", savedRoom.getId());
        
        // 3. 생성자를 멤버로 추가 (방장은 자동으로 참여)
        Member creator = Member.join(userId, savedRoom.getId());
        memberRepository.save(creator);
        log.debug("생성자 멤버 추가 완료 - userId: {}, roomId: {}", userId, savedRoom.getId());
        
        // 4. Redis 캐싱 (Write-Through 전략)
        CachedDiscussionRoom cached = CachedDiscussionRoom.fromDomain(savedRoom, 1); // 현재 인원 1명
        cacheRepository.saveNewRoomToRedis(cached, userId, System.currentTimeMillis());
        log.debug("Redis 캐싱 완료 - roomId: {}", savedRoom.getId());
        
        // 5. Response 반환
        log.info("논의방 생성 성공 - roomId: {}", savedRoom.getId());
        return new CreateDiscussionRoomRes(
            savedRoom.getId(),
            savedRoom.getTitle(),
            savedRoom.getDescription(),
            1, // 현재 인원 1명 (생성자)
            savedRoom.getAccessLevel()
        );
    }
}
