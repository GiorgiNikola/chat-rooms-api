package com.giorgi.chatroomsapi.dto.roommembership;

import com.giorgi.chatroomsapi.enums.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RoomMembershipResponseDto {

    private Long id;

    private Long roomId;

    private Long userId;

    private Role role;

    private LocalDateTime joinedAt;
}
