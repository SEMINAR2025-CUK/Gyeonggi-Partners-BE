package org.example.gyeonggi_partners.domain.user.infra.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.gyeonggi_partners.domain.common.BaseEntity;
import org.example.gyeonggi_partners.domain.discussionRoom.infra.persistence.member.MemberEntity;
import org.example.gyeonggi_partners.domain.user.domain.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * User JPA 엔티티
 * DB와 매핑되는 영속성 객체
 */
@Entity
@Table(name = "users")  // user → users로 수정
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true, length = 30)
    private String loginId;

    @Column(name = "login_pw", nullable = false, length = 255)
    private String loginPw;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "email", nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberEntity> members = new ArrayList<>();

    @Builder
    private UserEntity(Long id, String loginId, String loginPw, String name,
                      String nickname, String email, String phoneNumber, String role) {
        this.id = id;
        this.loginId = loginId;
        this.loginPw = loginPw;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    /**
     * 도메인 모델을 엔티티로 변환 (Domain -> Entity)
     */
    public static UserEntity fromDomain(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .loginPw(user.getLoginPw())
                .name(user.getName())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .build();
    }

    /**
     * 엔티티를 도메인 모델로 변환 (Entity -> Domain)
     */
    public User toDomain() {
        return User.restore(
                this.id,
                this.loginId,
                this.loginPw,
                this.name,
                this.nickname,
                this.email,
                this.phoneNumber,
                this.role,
                this.getCreatedAt(),
                this.getUpdatedAt(),
                this.getDeletedAt()
        );
    }
}
