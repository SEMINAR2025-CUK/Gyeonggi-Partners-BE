package org.example.gyeonggi_partners.domain.discussionRoom.infra.cache.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.model.AccessLevel;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.model.DiscussionRoom;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.model.Region;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 캐시에 저장될 논의방 데이터 전송 객체
 * 
 * <p>결정사항 1-1에 따라 다음 필드만 캐싱:</p>
 * <ul>
 *   <li>id, title, region, accessLevel, createdAt</li>
 *   <li>currentUsers (1-2 결정: 추가)</li>
 *   <li>description 제외 (메모리 절약)</li>
 *   <li>deletedAt 제외 (삭제된 방은 캐시에서 즉시 제거)</li>
 * </ul>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CachedDiscussionRoom {
    
    private Long id;
    private String title;
    private String region;           // Enum을 문자열로 저장 (결정사항 1-3)
    private String accessLevel;      // Enum을 문자열로 저장
    private String createdAt;        // ISO-8601 형식 (결정사항 1-4)
    private Integer currentUsers;    // 현재 참여 인원 수 (결정사항 1-2)
    
    /**
     * 도메인 모델을 캐시 DTO로 변환
     * 
     * @param domain 논의방 도메인 모델
     * @param currentUsers 현재 참여 인원 수 (DB에서 COUNT한 값)
     * @return 캐시용 DTO
     */
    public static CachedDiscussionRoom fromDomain(DiscussionRoom domain, int currentUsers) {
        return CachedDiscussionRoom.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .region(domain.getRegion().name())
                .accessLevel(domain.getAccessLevel().name())
                .createdAt(domain.getCreatedAt().toString())  // ISO-8601
                .currentUsers(currentUsers)
                .build();
    }
    
    /**
     * 캐시 DTO를 도메인 모델로 변환
     * 
     * @return 논의방 도메인 모델
     */
    public DiscussionRoom toDomain() {
        return DiscussionRoom.restore(
                this.id,
                this.title,
                null,  // description은 캐시에 없음
                Region.valueOf(this.region),
                AccessLevel.valueOf(this.accessLevel),
                LocalDateTime.parse(this.createdAt),
                null,  // updatedAt은 캐시에 없음
                null   // deletedAt은 캐시에 없음
        );
    }
    
    /**
     * Redis Hash에 저장하기 위한 Map 변환
     * 
     * @return Hash 필드-값 맵
     */
    public Map<String, String> toHashMap() {
        Map<String, String> map = new HashMap<>();
        map.put("id", String.valueOf(this.id));
        map.put("title", this.title);
        map.put("region", this.region);
        map.put("accessLevel", this.accessLevel);
        map.put("createdAt", this.createdAt);
        map.put("currentUsers", String.valueOf(this.currentUsers));
        return map;
    }
    
    /**
     * Redis Hash에서 가져온 Map을 DTO로 변환
     * 
     * @param map Redis Hash 데이터
     * @return 캐시용 DTO
     */
    public static CachedDiscussionRoom fromHashMap(Map<Object, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        
        return CachedDiscussionRoom.builder()
                .id(Long.valueOf(map.get("id").toString()))
                .title(map.get("title").toString())
                .region(map.get("region").toString())
                .accessLevel(map.get("accessLevel").toString())
                .createdAt(map.get("createdAt").toString())
                .currentUsers(Integer.valueOf(map.get("currentUsers").toString()))
                .build();
    }
}
