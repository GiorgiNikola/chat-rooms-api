package com.giorgi.chatroomsapi.repository;

import com.giorgi.chatroomsapi.entity.RoomMembership;
import com.giorgi.chatroomsapi.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomMembershipRepository extends JpaRepository<RoomMembership, Long> {
    Optional<RoomMembership> findByRoomIdAndUserId(Long roomId, Long userId);
    List<RoomMembership> findByRoomId(Long roomId);
    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    @Query("""
        SELECT CASE WHEN COUNT(rm) > 0 THEN true ELSE false END
        FROM RoomMembership rm
        WHERE rm.room.id = :roomId AND rm.user.id = :userId AND rm.role = :role
    """)
    boolean userHasRole(@Param("roomId") Long roomId, @Param("userId") Long userId, @Param("role") Role role);
}
