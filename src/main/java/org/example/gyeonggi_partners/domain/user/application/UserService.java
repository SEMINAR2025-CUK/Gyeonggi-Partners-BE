package org.example.gyeonggi_partners.domain.user.application;

import lombok.RequiredArgsConstructor;
import org.example.gyeonggi_partners.common.exception.BusinessException;
import org.example.gyeonggi_partners.domain.user.api.dto.SignUpRequest;
import org.example.gyeonggi_partners.domain.user.api.dto.SignUpResponse;
import org.example.gyeonggi_partners.domain.user.domain.model.User;
import org.example.gyeonggi_partners.domain.user.domain.repository.UserRepository;
import org.example.gyeonggi_partners.domain.user.exception.UserErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User 애플리케이션 서비스
 * 회원가입 등 유즈케이스 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {


    private final EmailVerificationService emailVerificationService;

    /**
     * 이메일 인증번호 발송
     * @param email 이메일
     */
    public void sendEmailVerification(String email) {
        emailVerificationService.sendVerificationCode(email);
    }

    /**
     * 이메일 인증번호 검증
     * @param email 이메일
     * @param code 인증번호
     */
    public void verifyEmail(String email, String code) {
        emailVerificationService.verifyCode(email, code);
    }


}
