package org.example.gyeonggipartners.domain.proposal.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.gyeonggipartners.domain.proposal.domain.model.Evidence;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class UpdateProposalRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(min = 5, max = 100, message = "제목은 5자 이상 100자 이하로 입력해주세요.")
    private String title;

    @NotBlank(message = "문제 개요는 필수입니다.")
    private String problemOverview;

    @NotBlank(message = "해결 방안은 필수입니다.")
    private String solution;

    @Valid
    private List<EvidenceDto> evidences;

    public List<Evidence> toEvidenceDomains() {
        if (evidences == null) return new ArrayList<>();
        return evidences.stream()
                .map(EvidenceDto::toDomain)
                .toList();
    }
}