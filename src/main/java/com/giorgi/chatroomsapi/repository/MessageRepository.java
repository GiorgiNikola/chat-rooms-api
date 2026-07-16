package com.giorgi.chatroomsapi.repository;

import com.giorgi.chatroomsapi.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByRoomId(Long roomId, Pageable pageable);
}
