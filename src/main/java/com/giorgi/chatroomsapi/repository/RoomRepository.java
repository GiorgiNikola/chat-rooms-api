package com.giorgi.chatroomsapi.repository;

import com.giorgi.chatroomsapi.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM Room r
        WHERE r.id = :roomId AND r.owner.email = :email
    """)
    boolean isRoomOwner(@Param("roomId") Long roomId, @Param("email") String email);
}
