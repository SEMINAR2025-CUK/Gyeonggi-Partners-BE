package org.example.gyeonggi_partners.domain.message.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.gyeonggi_partners.domain.discussionRoom.infra.persistence.DiscussionRoomEntity;
import org.example.gyeonggi_partners.domain.user.infra.persistence.UserEntity;

@Getter
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message{

    private Long id;
    private UserEntity user;

    // 논의방 추가시 주석해제 예정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="room_id", nullable=false)
    private DiscussionRoomEntity room;

    private String content;



}
