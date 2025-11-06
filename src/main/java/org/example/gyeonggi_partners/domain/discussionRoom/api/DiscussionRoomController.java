package org.example.gyeonggi_partners.domain.discussionRoom.api;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.gyeonggi_partners.common.jwt.CustomUserDetails;
import org.example.gyeonggi_partners.domain.discussionRoom.api.dto.CreateDiscussionRoomReq;
import org.example.gyeonggi_partners.domain.discussionRoom.api.dto.CreateDiscussionRoomRes;
import org.example.gyeonggi_partners.domain.discussionRoom.application.DiscussionRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "논의방 API", description = "논의방 생성, 조회, 입장, 삭제 관련 API")
@RestController
@RequestMapping("/api/discussion-rooms")
@RequiredArgsConstructor
public class DiscussionRoomController {

    private final DiscussionRoomService discussionRoomService;

    /**
     * 논의방 생성
     * 
     * @param request 논의방 생성 요청 데이터
     * @param userDetails 현재 로그인한 사용자 정보 (Spring Security)
     * @return 생성된 논의방 정보
     */
    @Operation(
            summary = "논의방 생성",
            description = "새로운 논의방을 생성합니다. 생성자는 자동으로 방에 참여됩니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/create")
    public ResponseEntity<CreateDiscussionRoomRes> createRoom(
            @Valid @RequestBody CreateDiscussionRoomReq request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        System.out.println("userDetails: " + userDetails);
        CreateDiscussionRoomRes response = discussionRoomService.createDiscussionRoom(
                request, 
                userDetails.getUserId()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
