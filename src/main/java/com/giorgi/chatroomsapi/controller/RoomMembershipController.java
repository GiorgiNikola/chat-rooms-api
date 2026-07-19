package com.giorgi.chatroomsapi.controller;

import com.giorgi.chatroomsapi.dto.roommembership.RoomMembershipResponseDto;
import com.giorgi.chatroomsapi.service.RoomMembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/rooms/{roomId}/members")
public class RoomMembershipController {

    private final RoomMembershipService roomMembershipService;

    @PostMapping
    public ResponseEntity<RoomMembershipResponseDto> join(@PathVariable Long roomId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomMembershipService.joinRoom(roomId));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> leave(@PathVariable Long roomId) {
        roomMembershipService.leaveRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/promote")
    public ResponseEntity<RoomMembershipResponseDto> promote(@PathVariable Long roomId, @PathVariable Long userId) {
        return ResponseEntity.ok(roomMembershipService.promote(roomId, userId));
    }

    @PatchMapping("/{userId}/demote")
    public ResponseEntity<RoomMembershipResponseDto> demote(@PathVariable Long roomId, @PathVariable Long userId) {
        return ResponseEntity.ok(roomMembershipService.demote(roomId, userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> kick(@PathVariable Long roomId, @PathVariable Long userId) {
        roomMembershipService.kick(roomId, userId);
        return ResponseEntity.noContent().build();
    }
}