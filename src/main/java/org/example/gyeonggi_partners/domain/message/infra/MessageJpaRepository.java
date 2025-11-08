package org.example.gyeonggi_partners.domain.message.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageJpaRepository extends JpaRepository<MessageEntity, Long> {

    List<MessageEntity> findByDiscussionRoom_IdOrderByCreatedAtDescIdOrderByCreatedAtDesc(Long roomId);
}
