package org.example.gyeonggi_partners.domain.user.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User JPA Repository
 * Spring Data JPA 인터페이스
 */
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    /**
     * 로그인 ID 존재 여부 확인
     */
    boolean existsByLoginId(String loginId);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 전화번호 존재 여부 확인
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * 로그인 ID로 UserEntity 조회
     */
    Optional<UserEntity> findByLoginId(String loginId);
}
