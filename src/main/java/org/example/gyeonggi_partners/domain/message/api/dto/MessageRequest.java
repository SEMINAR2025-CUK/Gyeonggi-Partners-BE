package org.example.gyeonggi_partners.domain.message.api.dto;

import lombok.Getter;
import org.example.gyeonggi_partners.domain.message.api.MessageType;

@Getter
public class MessageRequest {
    private MessageType type;
    private String content;

    //논의방 추가시 주석 해제
    // private Long roomId;
    private Long userId;
}
