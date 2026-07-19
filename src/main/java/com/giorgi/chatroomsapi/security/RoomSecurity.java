package com.giorgi.chatroomsapi.security;

import com.giorgi.chatroomsapi.entity.User;
import com.giorgi.chatroomsapi.enums.Role;
import com.giorgi.chatroomsapi.exception.ResourceNotFoundException;
import com.giorgi.chatroomsapi.repository.RoomMembershipRepository;
import com.giorgi.chatroomsapi.repository.RoomRepository;
import com.giorgi.chatroomsapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RoomSecurity {

    private final RoomRepository roomRepository;
    private final RoomMembershipRepository roomMembershipRepository;
    private final UserRepository userRepository;

    public boolean isRoomOwner(Long roomId, Authentication auth) {
        return roomRepository.isRoomOwner(roomId, auth.getName());
    }

    public boolean isRoomOwnerOrModerator(Long roomId, Authentication auth) {
        if (isRoomOwner(roomId, auth)) {
            return true;
        }
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return roomMembershipRepository.userHasRole(roomId, user.getId(), Role.MODERATOR);
    }
}
