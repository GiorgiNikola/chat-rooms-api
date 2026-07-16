package com.giorgi.chatroomsapi.repository;

import com.giorgi.chatroomsapi.entity.RoomMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomMembershipRepository extends JpaRepository<RoomMembership, Long> {
    Optional<RoomMembership> findByRoomIdAndUserId(Long roomId, Long userId);
    List<RoomMembership> findByRoomId(Long roomId);
    boolean existsByRoomIdAndUserId(Long roomId, Long userId);
}
