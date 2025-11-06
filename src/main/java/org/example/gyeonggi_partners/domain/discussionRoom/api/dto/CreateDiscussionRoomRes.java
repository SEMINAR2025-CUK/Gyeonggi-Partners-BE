package org.example.gyeonggi_partners.domain.discussionRoom.api.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.gyeonggi_partners.domain.discussionRoom.domain.model.AccessLevel;

@Getter
@NoArgsConstructor
public class CreateDiscussionRoomRes {

    private Long roomId;
    private String title;
    private String description;
    private Integer membersCount;
    private AccessLevel accessLevel;

    public CreateDiscussionRoomRes(Long roomId, String title,
                                   String description, Integer membersCount,
                                   AccessLevel accessLevel
    ) {
        this.roomId = roomId;
        this.title = title;
        this.description = description;
        this.membersCount = membersCount;
        this.accessLevel = accessLevel;
    }
}
