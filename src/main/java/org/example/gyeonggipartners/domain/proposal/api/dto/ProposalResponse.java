package org.example.gyeonggipartners.domain.proposal.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.gyeonggipartners.domain.proposal.domain.model.Proposal;
import org.example.gyeonggipartners.domain.proposal.domain.model.ProposalStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProposalResponse {

    private Long id;
    private Long roomId;
    private Long lastModifierId; // 최근 수정자 ID

    private String title;
    private String problemOverview;
    private String solution;
    private List<EvidenceDto> evidences;

    private ProposalStatus status;
    private Integer requiredConsents;
    private LocalDateTime consentDeadline;

    private Long lockedBy;       // 현재 락 소유자 (없으면 null)
    private LocalDateTime lockedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProposalResponse from(Proposal proposal) {
        return ProposalResponse.builder()
                .id(proposal.getId())
                .roomId(proposal.getRoomId())
                .lastModifierId(proposal.getLastModifierId())
                .title(proposal.getTitle())
                .problemOverview(proposal.getProblemOverview())
                .solution(proposal.getSolution())
                .evidences(proposal.getEvidences() != null
                        ? proposal.getEvidences().stream().map(EvidenceDto::from).toList()
                        : List.of())
                .status(proposal.getStatus())
                .requiredConsents(proposal.getRequiredConsents())
                .consentDeadline(proposal.getConsentDeadline())
                .lockedBy(proposal.getLockedBy())
                .lockedAt(proposal.getLockedAt())
                .createdAt(proposal.getCreatedAt())
                .updatedAt(proposal.getUpdatedAt())
                .build();
    }
}