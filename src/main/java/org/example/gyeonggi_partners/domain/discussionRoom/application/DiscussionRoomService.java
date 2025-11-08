package org.example.gyeonggi_partners.domain.discussionRoom.application;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.gyeonggi_partners.common.exception.BusinessException;
import org.example.gyeonggi_partners.domain.discussionRoom.api.dto.*;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.model.DiscussionRoom;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.model.Member;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.repository.DiscussionRoomRepository;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.repository.MemberRepository;
import org.example.gyeonggi_partners.domain.discussionRoom.exception.DiscussionRoomErrorCode;
import org.example.gyeonggi_partners.domain.discussionRoom.infra.cache.DiscussionRoomCacheRepository;
import org.example.gyeonggi_partners.domain.discussionRoom.infra.cache.dto.CachedDiscussionRoom;
import org.example.gyeonggi_partners.domain.discussionRoom.infra.cache.dto.DiscussionRoomsPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    public JoinRoomRes joinRoom(Long userId, Long roomId) {
        log.info("논의방 입장 요청 - userId: {}, roomId: {}", userId, roomId);

        // 1. 중복 참여 확인
        if (memberRepository.existsByUserIdAndRoomId(userId, roomId)) {
            throw new BusinessException(DiscussionRoomErrorCode.ALREADY_JOINED_ROOM);
        }

        // 2. 방 정보를 캐시에서 조회, 캐시 미스 시 DB 조회하도록 수정. DB에도 없을 경우 에러 처리
        CachedDiscussionRoom cachedRoom = cacheRepository.retrieveCachingRoom(roomId)
                .orElseThrow(
                        () -> new BusinessException(DiscussionRoomErrorCode.ROOM_NOT_FOUND)
                );


        // 3. DB에 멤버 추가
        Member member = Member.join(userId, roomId);
        memberRepository.save(member);
        log.debug("멤버 추가 완료 - userId: {}, roomId: {}", userId, roomId);

        // 4. Redis 업데이트
        cacheRepository.addUserToRoom(userId, roomId, System.currentTimeMillis());
        log.debug("Redis 업데이트 완료 - roomId: {}", roomId);

        // 5. 멤버 목록 조회
        List<Long> memberIds = cacheRepository.retrieveRoomMembers(roomId);

        log.info("논의방 입장 성공 - userId: {}, roomId: {}", userId, roomId);
        return JoinRoomRes.of(cachedRoom, memberIds);
    }






    /**
     * 전체 논의방 목록 조회 (최신순, 페이징)
     * Cache-Aside 전략: Redis 먼저 조회 → 미스면 DB 조회
     * 
     * @param page 페이지 번호 (1부터 시작)
     * @param size 페이지 크기
     * @return 논의방 목록 및 페이징 정보
     */
    @Transactional(readOnly = true)
    public DiscussionRoomListRes retrieveTotalRooms(int page, int size) {
        log.info("전체 논의방 목록 조회 - page: {}, size: {}", page, size);
        
        // 1. Redis에서 논의방 ID 목록 조회 (페이징)
        // 페이지는 1부터 시작하므로 Redis 조회 시 0-based로 변환
        DiscussionRoomsPage pagedRoomIds = cacheRepository.retrieveTotalRoomsByPage(page - 1, size);
        
        if (pagedRoomIds.getRoomIds().isEmpty()) {
            log.debug("조회된 논의방 없음");
            return DiscussionRoomListRes.of(List.of(), page, size, 0);
        }
        
        // 2. Redis에서 각 논의방 상세 정보 조회
        List<CachedDiscussionRoom> cachedRooms = cacheRepository.retrieveTotalCachingRoom(pagedRoomIds.getRoomIds());
        
        // 3. DTO 변환
        List<DiscussionRoomInfo> roomSummaries = cachedRooms.stream()
                .map(DiscussionRoomInfo::from)
                .collect(Collectors.toList());
        
        log.info("전체 논의방 목록 조회 성공 - 조회된 방: {}개, 전체: {}개", 
                roomSummaries.size(), pagedRoomIds.getTotalRoomsCount());
        
        // 4. 페이징 정보와 함께 응답
        return DiscussionRoomListRes.of(
                roomSummaries,
                page,
                size,
                pagedRoomIds.getTotalRoomsCount()
        );
    }

    /**
     * 사용자가 참여한 논의방 목록 조회 (최신 참여순, 페이징)
     * Cache-Aside 전략: Redis 먼저 조회 → 미스면 DB 조회
     * 
     * @param userId 사용자 ID
     * @param page 페이지 번호 (1부터 시작)
     * @param size 페이지 크기
     * @return 논의방 목록 및 페이징 정보
     */
    @Transactional(readOnly = true)
    public DiscussionRoomListRes retrieveMyJoinedRooms(Long userId, int page, int size) {
        log.info("내가 참여한 논의방 목록 조회 - userId: {}, page: {}, size: {}", userId, page, size);
        
        // 1. Redis에서 사용자가 참여한 논의방 ID 목록 조회 (페이징)
        DiscussionRoomsPage pagedRoomIds = cacheRepository.retrieveJoinedRoomsByPage(userId, page - 1, size);
        
        if (pagedRoomIds.getRoomIds().isEmpty()) {
            log.debug("참여한 논의방 없음 - userId: {}", userId);
            return DiscussionRoomListRes.of(List.of(), page, size, 0);
        }
        
        // 2. Redis에서 각 논의방 상세 정보 조회
        List<CachedDiscussionRoom> cachedRooms = cacheRepository.retrieveTotalCachingRoom(pagedRoomIds.getRoomIds());
        
        // 3. DTO 변환
        List<DiscussionRoomInfo> roomSummaries = cachedRooms.stream()
                .map(DiscussionRoomInfo::from)
                .collect(Collectors.toList());
        
        log.info("내가 참여한 논의방 목록 조회 성공 - userId: {}, 조회된 방: {}개, 전체: {}개", 
                userId, roomSummaries.size(), pagedRoomIds.getTotalRoomsCount());
        
        // 4. 페이징 정보와 함께 응답
        return DiscussionRoomListRes.of(
                roomSummaries,
                page,
                size,
                pagedRoomIds.getTotalRoomsCount()
        );
    }

    public void leaveRoom(Long userId, Long roomId) {
        log.info("논의방 나가기 요청 - userId: {}, roomId: {}", userId, roomId);
        
        // 1. Redis 퇴장 처리
        cacheRepository.removeUserFromRoom(userId, roomId);
        
        // 2. DB 멤버 삭제
        memberRepository.deleteByUserIdAndRoomId(userId, roomId);
        
        // 3. 남은 인원 확인
        int remainingUsers = memberRepository.countByRoomId(roomId);
        log.debug("남은 인원 - roomId: {}, count: {}", roomId, remainingUsers);
        
        // 4. 마지막 사람이면 방 삭제
        if (remainingUsers == 0) {
            log.info("마지막 멤버 퇴장 - 방 삭제 처리 - roomId: {}", roomId);
            discussionRoomRepository.softDelete(roomId);
            
            // creatorId는 알 수 없으므로 Redis만 부분 삭제
            cacheRepository.evictRoomCache(roomId, null);
        }
        
        log.info("논의방 나가기 성공 - userId: {}, roomId: {}", userId, roomId);
    }
}
