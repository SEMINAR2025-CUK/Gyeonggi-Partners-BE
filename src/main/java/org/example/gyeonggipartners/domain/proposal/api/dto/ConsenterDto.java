package org.example.gyeonggipartners.domain.proposal.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsenterDto {
    private Long userId;
    private String nickname;
}