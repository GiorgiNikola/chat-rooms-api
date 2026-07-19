package com.giorgi.chatroomsapi.service;

import com.giorgi.chatroomsapi.dto.roommembership.RoomMembershipResponseDto;
import com.giorgi.chatroomsapi.entity.Room;
import com.giorgi.chatroomsapi.entity.RoomMembership;
import com.giorgi.chatroomsapi.entity.User;
import com.giorgi.chatroomsapi.enums.Role;
import com.giorgi.chatroomsapi.exception.DuplicateResourceException;
import com.giorgi.chatroomsapi.exception.InvalidOperationException;
import com.giorgi.chatroomsapi.exception.ResourceNotFoundException;
import com.giorgi.chatroomsapi.mapper.RoomMembershipMapper;
import com.giorgi.chatroomsapi.repository.RoomMembershipRepository;
import com.giorgi.chatroomsapi.repository.RoomRepository;
import com.giorgi.chatroomsapi.repository.UserRepository;
import com.giorgi.chatroomsapi.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RoomMembershipService {

    private final RoomMembershipRepository roomMembershipRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final RoomMembershipMapper roomMembershipMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    private Room getRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }

    @Transactional
    public RoomMembershipResponseDto joinRoom(Long roomId) {
        User user = authenticatedUserProvider.getCurrentUser();
        if (roomMembershipRepository.existsByRoomIdAndUserId(roomId, user.getId())) {
            throw new DuplicateResourceException("User is already a member of the room");
        }
        Room room = getRoomById(roomId);
        RoomMembership roomMembership = new RoomMembership();
        roomMembership.setRoom(room);
        roomMembership.setUser(user);
        roomMembership.setRole(Role.MEMBER);
        RoomMembership saved = roomMembershipRepository.save(roomMembership);
        return roomMembershipMapper.toRoomMembershipResponseDto(saved);
    }

    @Transactional
    public void leaveRoom(Long roomId) {
        User user = authenticatedUserProvider.getCurrentUser();
        RoomMembership roomMembership = roomMembershipRepository.findByRoomIdAndUserId(roomId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of the room"));
        if (roomMembership.getRole() == Role.OWNER) {
            throw new InvalidOperationException("Owner cannot leave the room. Please transfer ownership or delete the room.");
        }
        roomMembershipRepository.delete(roomMembership);
    }

    @Transactional
    @PreAuthorize("@roomSecurity.isRoomOwner(#roomId, authentication)")
    public RoomMembershipResponseDto promote(Long roomId, Long userId) {
        return changeRole(roomId, userId, Role.MODERATOR);
    }

    @Transactional
    @PreAuthorize("@roomSecurity.isRoomOwner(#roomId, authentication)")
    public RoomMembershipResponseDto demote(Long roomId, Long userId) {
        return changeRole(roomId, userId, Role.MEMBER);
    }

    private RoomMembershipResponseDto changeRole(Long roomId, Long userId, Role newRole) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room not found");
        }
        RoomMembership roomMembership = roomMembershipRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of the room"));

        if (roomMembership.getRole() == Role.OWNER) {
            throw new InvalidOperationException("Cannot change the role of the room owner.");
        }
        if (roomMembership.getRole() == newRole) {
            throw new InvalidOperationException("User already has the role " + newRole + ".");
        }

        roomMembership.setRole(newRole);
        RoomMembership saved = roomMembershipRepository.save(roomMembership);
        return roomMembershipMapper.toRoomMembershipResponseDto(saved);
    }

    @Transactional
    @PreAuthorize("@roomSecurity.isRoomOwnerOrModerator(#roomId, authentication)")
    public void kick(Long roomId, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room not found");
        }
        RoomMembership roomMembership = roomMembershipRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of the room"));
        if (roomMembership.getRole() == Role.OWNER) {
            throw new InvalidOperationException("Cannot kick the room owner.");
        }
        roomMembershipRepository.delete(roomMembership);
    }
}
