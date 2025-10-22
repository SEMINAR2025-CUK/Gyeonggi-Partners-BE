package org.example.gyeonggi_partners.domain.user.domain.repository;

import org.example.gyeonggi_partners.domain.user.domain.model.User;

/**
 * User 도메인 Repository 인터페이스
 * 도메인 계층에서 정의하고, 인프라 계층에서 구현
 */
public interface UserRepository {

    /**
     * 사용자 저장 (회원가입)
     * @param user 저장할 사용자
     * @return 저장된 사용자 (ID 포함)
     */
    User save(User user);

    /**
     * 로그인 ID 중복 확인
     * @param loginId 로그인 ID
     * @return 존재 여부
     */
    boolean existsByLoginId(String loginId);

    /**
     * 이메일 중복 확인
     * @param email 이메일
     * @return 존재 여부
     */
    boolean existsByEmail(String email);

    /**
     * 전화번호 중복 확인
     * @param phoneNumber 전화번호
     * @return 존재 여부
     */
    boolean existsByPhoneNumber(String phoneNumber);
}
