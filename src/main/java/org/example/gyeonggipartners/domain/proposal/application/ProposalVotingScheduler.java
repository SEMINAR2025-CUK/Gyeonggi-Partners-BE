package org.example.gyeonggipartners.domain.proposal.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProposalVotingScheduler {

    private final ProposalService proposalService;

    /**
     * 매 10분마다 투표 마감 기한이 지난 제안서를 확인하여 종료 처리
     * (cron 주기 등은 운영 정책에 따라 변경)
     */
    @Scheduled(cron = "0 0/10 * * * *")
    public void checkDeadline() {
        try {
            int closedCount = proposalService.closeExpiredProposals();
            if (closedCount > 0) {
                log.info("[Scheduler] {}개의 만료된 제안서를 투표 종료(제출 대기) 처리했습니다.", closedCount);
            }
        } catch (Exception e) {
            log.error("[Scheduler] 제안서 투표 마감 배치 작업 중 오류 발생", e);
        }
    }
}