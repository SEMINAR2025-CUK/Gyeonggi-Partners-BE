package org.example.gyeonggi_partners.domain.proposal.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.gyeonggi_partners.common.dto.ApiResponse;
import org.example.gyeonggi_partners.common.jwt.CustomUserDetails;
import org.example.gyeonggi_partners.domain.proposal.api.dto.CreateProposalRequest;
import org.example.gyeonggi_partners.domain.proposal.api.dto.ProposalResponse;
import org.example.gyeonggi_partners.domain.proposal.api.dto.UpdateProposalRequest;
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

    @Operation(summary = "제안서 생성", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<ApiResponse<ProposalResponse>> createProposal(
            @Valid @RequestBody CreateProposalRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        ProposalResponse response = proposalService.createProposal(request, userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(response, "제안서 생성 성공"));
    }


    @Operation(summary = "제안서 조회")
    @GetMapping("/{proposalId}")
    public ResponseEntity<ApiResponse<ProposalResponse>> getProposal(
            @PathVariable Long proposalId) {

        ProposalResponse response = proposalService.getProposal(proposalId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }


    @Operation(summary = "제안서 수정", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{proposalId}")
    public ResponseEntity<ApiResponse<ProposalResponse>> updateProposal(
            @PathVariable Long proposalId,
            @Valid @RequestBody UpdateProposalRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        ProposalResponse response = proposalService.updateProposal(proposalId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "제안서 수정 성공"));
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
}
