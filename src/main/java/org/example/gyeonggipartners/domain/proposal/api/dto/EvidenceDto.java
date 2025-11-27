package org.example.gyeonggipartners.domain.proposal.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.gyeonggipartners.domain.proposal.domain.model.Evidence;
import org.example.gyeonggipartners.domain.proposal.domain.model.EvidenceType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceDto {

    @NotNull(message = "증거자료 타입은 필수입니다.")
    private EvidenceType type;

    @NotBlank(message = "증거자료 제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자를 넘을 수 없습니다.")
    private String title;

    @NotBlank(message = "URL은 필수입니다.")
    @Size(max = 500, message = "URL은 500자를 넘을 수 없습니다.")
    private String url;

    // Domain Entity -> DTO 변환
    public static EvidenceDto from(Evidence evidence) {
        return EvidenceDto.builder()
                .type(evidence.getType())
                .title(evidence.getTitle())
                .url(evidence.getUrl())
                .build();
    }

    // DTO -> Domain Entity 변환
    public Evidence toDomain() {
        return Evidence.of(this.type, this.title, this.url);
    }
}