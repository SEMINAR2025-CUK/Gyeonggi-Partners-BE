package org.example.gyeonggi_partners.domain.message.infra;

import org.example.gyeonggi_partners.domain.discussionRoom.infra.persistence.DiscussionRoomEntity;
import org.example.gyeonggi_partners.domain.discussionRoom.infra.persistence.discussionRoom.DiscussionRoomJpaRepository;
import org.example.gyeonggi_partners.domain.user.infra.persistence.UserEntity;
import org.example.gyeonggi_partners.domain.user.infra.persistence.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("MessageRepository 테스트")
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private DiscussionRoomJpaRepository discussionRoomJpaRepository;

    private UserEntity testUser;
    private DiscussionRoomEntity testRoom;

    @BeforeEach
    void setUp() {
        // 테스트 유저 생성
        testUser = userJpaRepository.save(UserEntity.builder()
                .loginId("testuser")
                .loginPw("password123")
                .email("test@example.com")
                .name("테스트유저")
                .nickname("테스트닉네임")
                .phoneNumber("010-1234-5678")
                .role("USER")
                .build());

        // 테스트 논의방 생성
        testRoom = discussionRoomJpaRepository.save(DiscussionRoomEntity.builder()
                .title("테스트 논의방")
                .description("테스트용 논의방입니다.")
                .region(org.example.gyeonggi_partners.domain.discussionRoom.domain.model.Region.SUWON)
                .accessLevel(org.example.gyeonggi_partners.domain.discussionRoom.domain.model.AccessLevel.PUBLIC)
                .memberCount(1)
                .build());
    }

    @Test
    @DisplayName("최근 메시지 조회 - 정렬 순서 확인")
    void findLatestMessages_OrderDesc() {
        // given
        for (int i = 1; i <= 10; i++) {
            messageRepository.save(MessageEntity.builder()
                    .content("메시지 " + i)
                    .user(testUser)
                    .discussionRoom(testRoom)
                    .build());
        }

        // when
        List<MessageEntity> messages = messageRepository.findLatesetMessages(
                testRoom.getId(),
                PageRequest.of(0, 5)
        );

        // then
        assertThat(messages).hasSize(5);
        assertThat(messages.get(0).getContent()).isEqualTo("메시지 10");
        assertThat(messages.get(4).getContent()).isEqualTo("메시지 6");
    }

    @Test
    @DisplayName("최근 메시지 조회 - User Fetch Join 확인")
    void findLatestMessages_FetchJoinUser() {
        // given
        messageRepository.save(MessageEntity.builder()
                .content("테스트 메시지")
                .user(testUser)
                .discussionRoom(testRoom)
                .build());

        // when
        List<MessageEntity> messages = messageRepository.findLatesetMessages(
                testRoom.getId(),
                PageRequest.of(0, 10)
        );

        // then
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getUser().getName()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("커서 기반 메시지 조회 - 특정 ID 이전 메시지만 조회")
    void findMessagesBeforeCursor() {
        // given
        MessageEntity message1 = messageRepository.save(createMessage("메시지 1"));
        MessageEntity message2 = messageRepository.save(createMessage("메시지 2"));
        MessageEntity message3 = messageRepository.save(createMessage("메시지 3"));
        MessageEntity message4 = messageRepository.save(createMessage("메시지 4"));
        MessageEntity message5 = messageRepository.save(createMessage("메시지 5"));

        // when
        List<MessageEntity> messages = messageRepository.findMessagesBeforeCursor(
                testRoom.getId(),
                message4.getId(), // message4 이전 메시지들만 조회
                PageRequest.of(0, 10)
        );

        // then
        assertThat(messages).hasSize(3);
        assertThat(messages.get(0).getId()).isEqualTo(message3.getId());
        assertThat(messages.get(1).getId()).isEqualTo(message2.getId());
        assertThat(messages.get(2).getId()).isEqualTo(message1.getId());
    }

    @Test
    @DisplayName("커서 기반 메시지 조회 - 페이지 크기 제한")
    void findMessagesBeforeCursor_WithLimit() {
        // given
        MessageEntity lastMessage = null;
        for (int i = 1; i <= 10; i++) {
            lastMessage = messageRepository.save(createMessage("메시지 " + i));
        }

        // when
        List<MessageEntity> messages = messageRepository.findMessagesBeforeCursor(
                testRoom.getId(),
                lastMessage.getId(),
                PageRequest.of(0, 5)
        );

        // then
        assertThat(messages).hasSize(5);
    }

    @Test
    @DisplayName("다른 논의방의 메시지는 조회되지 않음")
    void findMessages_DifferentRoom() {
        // given
        DiscussionRoomEntity otherRoom = discussionRoomJpaRepository.save(
                DiscussionRoomEntity.builder()
                        .title("다른 논의방")
                        .description("다른 논의방입니다.")
                        .region(org.example.gyeonggi_partners.domain.discussionRoom.domain.model.Region.SEONGNAM)
                        .accessLevel(org.example.gyeonggi_partners.domain.discussionRoom.domain.model.AccessLevel.PUBLIC)
                        .memberCount(1)
                        .build()
        );

        messageRepository.save(MessageEntity.builder()
                .content("테스트방 메시지")
                .user(testUser)
                .discussionRoom(testRoom)
                .build());

        messageRepository.save(MessageEntity.builder()
                .content("다른방 메시지")
                .user(testUser)
                .discussionRoom(otherRoom)
                .build());

        // when
        List<MessageEntity> messages = messageRepository.findLatesetMessages(
                testRoom.getId(),
                PageRequest.of(0, 10)
        );

        // then
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getContent()).isEqualTo("테스트방 메시지");
    }

    @Test
    @DisplayName("메시지가 없을 때 빈 리스트 반환")
    void findMessages_EmptyRoom() {
        // when
        List<MessageEntity> messages = messageRepository.findLatesetMessages(
                testRoom.getId(),
                PageRequest.of(0, 10)
        );

        // then
        assertThat(messages).isEmpty();
    }

    private MessageEntity createMessage(String content) {
        return MessageEntity.builder()
                .content(content)
                .user(testUser)
                .discussionRoom(testRoom)
                .build();
    }
}
