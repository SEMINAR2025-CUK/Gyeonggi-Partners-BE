package org.example.gyeonggi_partners.domain.proposal.application;

import lombok.RequiredArgsConstructor;
import org.example.gyeonggi_partners.common.exception.BusinessException;
import org.example.gyeonggi_partners.domain.proposal.api.dto.ConsenterListResponse;
import org.example.gyeonggi_partners.domain.proposal.api.dto.CreateProposalRequest;
import org.example.gyeonggi_partners.domain.proposal.api.dto.ProposalResponse;
import org.example.gyeonggi_partners.domain.proposal.api.dto.UpdateProposalRequest;
import org.example.gyeonggi_partners.domain.proposal.domain.model.Consenter;
import org.example.gyeonggi_partners.domain.proposal.domain.model.ContentFormat;
import org.example.gyeonggi_partners.domain.proposal.domain.model.Proposal;
import org.example.gyeonggi_partners.domain.proposal.domain.model.SubmitStatus;
import org.example.gyeonggi_partners.domain.proposal.domain.repository.ProposalRepository;
import org.example.gyeonggi_partners.domain.proposal.exception.ProposalErrorCode;
import org.example.gyeonggi_partners.domain.user.domain.model.User;
import org.example.gyeonggi_partners.domain.user.domain.repository.UserRepository;
import org.example.gyeonggi_partners.domain.user.exception.UserErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private UserRepository userRepository;

    /**
     * 제안서 작성
     */
    public ProposalResponse createProposal(CreateProposalRequest request) {

        ContentFormat contents = ContentFormat.of(
                request.getParagraph(),
                request.getImage(),
                request.getSolution(),
                request.getExpectedEffect()
        );

        Proposal proposal = Proposal.create(request.getRoomId(), request.getTitle(), contents);

        Proposal saved = proposalRepository.save(proposal);

        return ProposalResponse.from(saved);
    }

    /**
     * 제안서 조회
     */
    @Transactional(readOnly = true)
    public ProposalResponse getProposal(Long proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new BusinessException(ProposalErrorCode.PROPOSAL_NOT_FOUND));

        return ProposalResponse.from(proposal);
    }

    /**
     * 제안서 수정
     */
    public ProposalResponse updateProposal(Long proposalId, UpdateProposalRequest request) {

        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new BusinessException(ProposalErrorCode.PROPOSAL_NOT_FOUND));

        if (proposal.getStatus() == SubmitStatus.VOTING) {
            throw new BusinessException(ProposalErrorCode.PROPOSAL_LOCKED);
        }

        ContentFormat contents = ContentFormat.of(
                request.getParagraph(),
                request.getImage(),
                request.getSolution(),
                request.getExpectedEffect()
        );

        proposal.update(request.getTitle(), contents);

        Proposal updated = proposalRepository.save(proposal);

        return ProposalResponse.from(updated);
    }

    /**
     * 투표 시작
     */
    public void startSubmission(Long proposalId, Long userId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new BusinessException(ProposalErrorCode.PROPOSAL_NOT_FOUND));

        proposal.startSubmission();
        proposalRepository.save(proposal);
    }

    /**
     * 해당 제안서에 동의하기
     */
    public void consentProposal(Long proposalId, Long userId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new BusinessException(ProposalErrorCode.PROPOSAL_NOT_FOUND));


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Consenter consenter = new Consenter(user.getId(), user.getNickname());

        proposal.addConsent(consenter);

        proposalRepository.save(proposal);
    }

    /**
     * 제안서 동의자 목록 조회
     */
    @Transactional(readOnly = true)
    public ConsenterListResponse getConsenters(Long proposalId) {

        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new BusinessException(ProposalErrorCode.PROPOSAL_NOT_FOUND));

        return ConsenterListResponse.from(proposal);
    }

}
