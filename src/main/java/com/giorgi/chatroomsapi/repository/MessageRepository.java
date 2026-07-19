package com.giorgi.chatroomsapi.repository;

import com.giorgi.chatroomsapi.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("""
    SELECT m FROM Message m
    WHERE m.room.id = :roomId
    AND m.deleted = false
    AND (:fromDate IS NULL OR m.sentAt >= :fromDate)
    AND (:toDate IS NULL OR m.sentAt <= :toDate)
    ORDER BY m.sentAt DESC
""")
    Page<Message> findByRoomIdAndDateRange(
            @Param("roomId") Long roomId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );
}
