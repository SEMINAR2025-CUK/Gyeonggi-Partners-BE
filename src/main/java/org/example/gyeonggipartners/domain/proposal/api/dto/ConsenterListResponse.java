package org.example.gyeonggipartners.domain.proposal.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ConsenterListResponse {

    private int totalCount;
    private List<ConsenterDto> consenters;

    public static ConsenterListResponse of(List<ConsenterDto> consenters) {
        return ConsenterListResponse.builder()
                .totalCount(consenters.size())
                .consenters(consenters)
                .build();
    }
}