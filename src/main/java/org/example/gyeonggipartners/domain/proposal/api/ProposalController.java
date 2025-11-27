package org.example.gyeonggipartners.domain.proposal.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.gyeonggipartners.domain.proposal.api.dto.*;
import org.example.gyeonggipartners.domain.proposal.application.ProposalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "제안서 API", description = "제안서 릴레이 편집, 투표, 조회 관련 API")
@RestController
@RequestMapping("/api/v1/proposals")
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;

    // =================================================================
    // 1. 조회 API (Read - Open to all room members)
    // =================================================================

    @Operation(
            summary = "논의방 제안서 목록 조회",
            description = "특정 논의방에 생성된 모든 제안서 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<List<ProposalResponse>> getProposalsByRoom(
            @Parameter(description = "논의방 ID", example = "1", required = true)
            @RequestParam Long roomId,

            // [수정] hidden = true 제거 -> Swagger에서 직접 입력 가능하도록 변경
            @Parameter(description = "사용자 ID (테스트용)", example = "1", required = true)
            @RequestHeader("X-USER-ID") Long userId
    ) {
        List<ProposalResponse> responses = proposalService.getProposalsByRoom(userId, roomId);
        return ResponseEntity.ok(responses);
    }

    @Operation(
            summary = "제안서 단건 상세 조회",
            description = "제안서의 상세 내용을 조회합니다. 현재 편집 락(Lock) 소유자 정보를 확인하여 편집 가능 여부를 판단할 수 있습니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{proposalId}")
    public ResponseEntity<ProposalResponse> getProposal(
            @Parameter(description = "제안서 ID", example = "1")
            @PathVariable Long proposalId,

            @Parameter(description = "사용자 ID", example = "1")
            @RequestHeader("X-USER-ID") Long userId
    ) {
        ProposalResponse response = proposalService.getProposal(userId, proposalId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "제안서 동의자 목록 조회",
            description = "해당 제안서에 투표(동의)한 사용자 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{proposalId}/consents")
    public ResponseEntity<ConsenterListResponse> getConsenters(
            @Parameter(description = "제안서 ID", example = "1")
            @PathVariable Long proposalId,

            @Parameter(description = "사용자 ID", example = "1")
            @RequestHeader("X-USER-ID") Long userId
    ) {
        ConsenterListResponse response = proposalService.getConsenters(userId, proposalId);
        return ResponseEntity.ok(response);
    }

    // =================================================================
    // 2. 편집 프로세스 (Edit Lifecycle)
    // =================================================================

    @Operation(
            summary = "[단계 1] 제안서 생성",
            description = "새로운 제안서를 생성합니다. 생성 즉시 작성자가 편집 락(Lock)을 획득하며 '작성 중(DRAFTING)' 상태가 됩니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<ProposalResponse> createProposal(
            @Valid @RequestBody CreateProposalRequest request,

            @Parameter(description = "사용자 ID", example = "1")
            @RequestHeader("X-USER-ID") Long userId
    ) {
        ProposalResponse response = proposalService.createProposal(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "[단계 3] 편집 모드 진입 (락 획득)",
            description = "임시 저장된 제안서 수정을 시작합니다. 비관적 락을 통해 동시 진입을 차단하며, 성공 시 편집 권한을 획득합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{proposalId}/edit/start")
    public ResponseEntity<ProposalResponse> startEditing(
            @Parameter(description = "제안서 ID", example = "1")
            @PathVariable Long proposalId,

            @Parameter(description = "사용자 ID", example = "1")
            @RequestHeader("X-USER-ID") Long userId
    ) {
        ProposalResponse response = proposalService.startEditing(userId, proposalId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "[단계 2/3] 제안서 내용 수정 (중간 저장)",
            description = "편집 중인 제안서의 내용을 업데이트합니다. 현재 락을 소유한 사용자만 호출 가능합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{proposalId}")
    public ResponseEntity<ProposalResponse> updateProposal(
            @Parameter(description = "제안서 ID", example = "1")
            @PathVariable Long proposalId,

            @Valid @RequestBody UpdateProposalRequest request,

            @Parameter(description = "사용자 ID", example = "1")
            @RequestHeader("X-USER-ID") Long userId
    ) {
        ProposalResponse response = proposalService.updateProposalContent(userId, proposalId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "[단계 2] 편집 종료 (임시 저장)",
            description = "편집을 종료하고 락을 반납합니다. 제안서 상태는 '임시 저장(SAVING)'으로 변경되어 타인이 접근 가능해집니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{proposalId}/edit/finish")
    public ResponseEntity<Void> finishEditing(
            @Parameter(description = "제안서 ID", example = "1")
            @PathVariable Long proposalId,

            @Parameter(description = "사용자 ID", example = "1")
            @RequestHeader("X-USER-ID") Long userId
    ) {
        proposalService.stopEditing(userId, proposalId);
        return ResponseEntity.ok().build();
    }

    // =================================================================
    // 3. 투표 프로세스 (Voting)
    // =================================================================

    @Operation(
            summary = "[단계 4] 작성 완료 (확정)",
            description = "제안서 작성을 최종 완료합니다. 상태가 '확정(COMPLETED)'으로 변경되며 더 이상 내용을 수정할 수 없습니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{proposalId}/complete")
    public ResponseEntity<Void> completeProposal(
            @Parameter(description = "제안서 ID", example = "1")
            @PathVariable Long proposalId,

            @Parameter(description = "사용자 ID", example = "1")
            @RequestHeader("X-USER-ID") Long userId
    ) {
        proposalService.completeProposal(userId, proposalId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "[단계 4] 투표 시작 설정",
            description = "확정된 제안서에 대해 최소 동의 인원과 마감 기한을 설정하고 투표를 시작합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{proposalId}/vote/start")
    public ResponseEntity<ProposalResponse> startVoting(
            @Parameter(description = "제안서 ID", example = "1")
            @PathVariable Long proposalId,

            @Valid @RequestBody StartVotingRequest request,

            @Parameter(description = "사용자 ID", example = "1")
            @RequestHeader("X-USER-ID") Long userId
    ) {
        ProposalResponse response = proposalService.startVoting(userId, proposalId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "[단계 5] 제안서 동의 (투표)",
            description = "제안서에 동의(투표)합니다. 중복 투표는 불가능하며, 조건 달성 시 '제출 대기(READY_TO_SUBMIT)' 상태로 자동 전환됩니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{proposalId}/vote")
    public ResponseEntity<Void> consentProposal(
            @Parameter(description = "제안서 ID", example = "1")
            @PathVariable Long proposalId,

            @Parameter(description = "사용자 ID", example = "1")
            @RequestHeader("X-USER-ID") Long userId
    ) {
        proposalService.consentProposal(userId, proposalId);
        return ResponseEntity.ok().build();
    }
}