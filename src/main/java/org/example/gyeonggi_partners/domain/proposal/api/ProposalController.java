package org.example.gyeonggi_partners.domain.proposal.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.example.gyeonggi_partners.common.dto.ApiResponse;
import org.example.gyeonggi_partners.common.jwt.CustomUserDetails;
import org.example.gyeonggi_partners.domain.proposal.api.dto.*;
import org.example.gyeonggi_partners.domain.proposal.application.ProposalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Proposal", description = "제안서 API")
@RestController
@RequestMapping("/api/proposals")
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;

    @Operation(summary = "제안서 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<ProposalResponse>> createProposal(
            @Valid @RequestBody CreateProposalRequest request) {

        ProposalResponse response = proposalService.createProposal(request);

        return ResponseEntity.ok(ApiResponse.success(response, "제안서 생성 성공"));
    }


    @Operation(summary = "제안서 조회")
    @GetMapping("/{proposalId}")
    public ResponseEntity<ApiResponse<ProposalResponse>> getProposal(
            @PathVariable Long proposalId) {

        ProposalResponse response = proposalService.getProposal(proposalId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "제안서 편집 시작 (락 획득)", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{proposalId}/start-editing")
    public ResponseEntity<ApiResponse<ProposalResponse>> startEditing(
            @PathVariable Long proposalId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        ProposalResponse response = proposalService.startEditing(proposalId, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(null, "편집 시작."))
    }


    @Operation(summary = "제안서 편집 완료 (락 해제)", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{proposalId}/finish-editing")
    public ResponseEntity<ApiResponse<Void>> finishEditing(
            @PathVariable Long proposalId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        proposalService.finishEditing(proposalId, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(null, "편집 완료."));
    }

    @Operation(summary = "제안서 수정", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{proposalId}")
    public ResponseEntity<ApiResponse<ProposalResponse>> updateProposal(
            @PathVariable Long proposalId,
            @Valid @RequestBody UpdateProposalRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        ProposalResponse response = proposalService.updateProposal(proposalId, request, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(response, "제안서 수정 성공"));
    }

    @Operation(summary = "제안서 락 상태 확인")
    @GetMapping("/{proposalId}/lock-status")
    public ResponseEntity<ApiResponse<LockStatusResponse>> getLockStatus(
            @PathVariable Long proposalId) {

        LockStatusResponse response = proposalService.getLockStatus(proposalId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }


    @Operation(summary = "제안서 동의", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{proposalId}/consents")
    public ResponseEntity<ApiResponse<ProposalResponse>> consentProposal(
            @PathVariable Long proposalId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        proposalService.consentProposal(proposalId, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(null, "제안서 동의 완료"));
    }


    @Operation(summary = "제안서 동의자 목록 조회")
    @GetMapping("/{proposalId}/consenters")
    public ResponseEntity<ApiResponse<ConsenterListResponse>> getConsenters(
            @PathVariable Long proposalId) {

        ConsenterListResponse response = proposalService.getConsenters(proposalId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
