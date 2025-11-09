package org.example.gyeonggi_partners.domain.message.infra;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    MessageEntity save(MessageEntity message);

    // 최근 메세지 조회
    @Query("SELECT m FROM MessageEntity m " +
            "JOIN FETCH m.user " +
            "WHERE m.discussionRoom.id = :roomId " +
            "ORDER BY m.id DESC")
    List<MessageEntity> findLatesetMessages(Long roomdId, Pageable pageable);

    // 이전 메세지 조회, 커서 기반 페이징
    @Query("SELECT m FROM MessageEntity m " +
            "JOIN FETCH m.user " +
            "WHERE m.discussionRoom.id = :roomId " +
            "AND m.id < :cursor " +
            "ORDER BY m.id DESC")
    List<MessageEntity> findMessagesBeforeCursor(Long roomId, Long cursor, Pageable pageable);
}

