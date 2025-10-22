package org.example.gyeonggi_partners.domain.user.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * User 도메인 모델
 * JPA와 독립적인 순수 도메인 객체
 */
@Getter
public class User {

    //로그인 아이디 규칙
    private static final int MAX_LOGIN_ID_LENGTH = 20;

    //비밀번호 규칙
    private static final int MIN_PASSWORD_LENGTH = 8;  // 평문 기준
    private static final int MAX_PASSWORD_LENGTH = 50;

    //이름 규칙
    private static final int MAX_NAME_LENGTH = 20;

    //닉네임 규칙
    private static final int MAX_NICKNAME_LENGTH = 15;

    //이메일 규칙
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,30}$");
    private static final int MAX_EMAIL_LENGTH = 50;

    //전화번호 규칙
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^01[016789]-\\d{3,4}-\\d{4}$");
    private static final int MAX_PHONE_NUMBER_LENGTH = 20;


    private Long id;
    private String loginId;        // 로그인 ID
    private String loginPw;        // 암호화된 비밀번호
    private String name;           // 실명
    private String nickname;       // 닉네임
    private String email;          // 이메일
    private String phoneNumber;    // 전화번호
    private String role;           // USER, ADMIN 등
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private User(Long id, String loginId, String loginPw, String name,
                 String nickname, String email, String phoneNumber, String role,
                 LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.loginId = loginId;
        this.loginPw = loginPw;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role != null ? role : "USER";
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    /**
     * 새로운 사용자 생성 (회원가입)
     */
    public static User create(String loginId, String loginPw, String name,
                              String nickname, String email, String phoneNumber) {
        validateLoginId(loginId);
        validatePassword(loginPw);
        validateName(name);
        validateNickname(nickname);
        validateEmail(email);
        validatePhoneNumber(phoneNumber);

        return User.builder()
                .loginId(loginId)
                .loginPw(loginPw)  // Service에서 암호화해서 넘겨줘야 함
                .name(name)
                .nickname(nickname)
                .email(email)
                .phoneNumber(phoneNumber)
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 기존 사용자 복원 (DB에서 조회)
     */
    public static User restore(Long id, String loginId, String loginPw, String name,
                               String nickname, String email, String phoneNumber, String role,
                               LocalDateTime createdAt, LocalDateTime updatedAt, 
                               LocalDateTime deletedAt) {
        return User.builder()
                .id(id)
                .loginId(loginId)
                .loginPw(loginPw)
                .name(name)
                .nickname(nickname)
                .email(email)
                .phoneNumber(phoneNumber)
                .role(role)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .build();
    }

    // ==================== Validation Methods ====================

    private static void validateLoginId(String loginId) {
        if (loginId == null || loginId.trim().isEmpty()) {
            throw new IllegalArgumentException("로그인 ID는 필수입니다.");
        }
        if (loginId.length() > MAX_LOGIN_ID_LENGTH) {
            throw new IllegalArgumentException(
                String.format("로그인 ID는 %d자를 초과할 수 없습니다.", MAX_LOGIN_ID_LENGTH));
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                String.format("비밀번호는 %d자 이상이어야 합니다.", MIN_PASSWORD_LENGTH));
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("비밀번호는 %d자를 초과할 수 없습니다.", MAX_PASSWORD_LENGTH));
        }
        // 암호화 전 평문은 길이 제한 없음 (암호화 후 255자 이내로 저장됨)
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                String.format("이름은 %d자를 초과할 수 없습니다.", MAX_NAME_LENGTH));
        }
    }

    private static void validateNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }
        if (nickname.length() > MAX_NICKNAME_LENGTH) {
            throw new IllegalArgumentException(
                String.format("닉네임은 %d자를 초과할 수 없습니다.", MAX_NICKNAME_LENGTH));
        }
    }

    private static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        if (email.length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException(
                String.format("이메일은 %d자를 초과할 수 없습니다.", MAX_EMAIL_LENGTH));
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }
    }

    private static void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }
        if (phoneNumber.length() > MAX_PHONE_NUMBER_LENGTH) {
            throw new IllegalArgumentException(
                String.format("전화번호는 %d자를 초과할 수 없습니다.", MAX_PHONE_NUMBER_LENGTH));
        }
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("올바른 휴대폰 번호 형식이 아닙니다.");
        }
    }
}
