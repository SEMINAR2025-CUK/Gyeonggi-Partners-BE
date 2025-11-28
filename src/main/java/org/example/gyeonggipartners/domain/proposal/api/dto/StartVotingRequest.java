package org.example.gyeonggipartners.domain.proposal.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class StartVotingRequest {

    @NotNull(message = "최소 동의 인원은 필수입니다.")
    @Min(value = 1, message = "최소 1명 이상이어야 합니다.")
    private Integer requiredConsents;

    @NotNull(message = "투표 마감일은 필수입니다.")
    @Future(message = "마감일은 현재 시간보다 미래여야 합니다.")
    private LocalDateTime consentDeadline;
}