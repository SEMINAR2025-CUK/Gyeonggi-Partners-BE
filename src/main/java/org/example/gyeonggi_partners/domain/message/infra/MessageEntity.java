package org.example.gyeonggi_partners.domain.message.infra;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.gyeonggi_partners.domain.common.BaseEntity;
import org.example.gyeonggi_partners.domain.user.infra.persistence.UserEntity;

@Entity
@Getter
@Table(name = "messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private UserEntity user;

    // 논의방 추가시 주석해제 예정
/*    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="room_id", nullable=false)
    private Room room;*/

    @Column(columnDefinition = "TEXT")
    private String content;



}
