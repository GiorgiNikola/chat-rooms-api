package com.giorgi.chatroomsapi.service;

import com.giorgi.chatroomsapi.dto.message.MessageCreateRequest;
import com.giorgi.chatroomsapi.dto.message.MessageResponseDto;
import com.giorgi.chatroomsapi.entity.Message;
import com.giorgi.chatroomsapi.entity.Room;
import com.giorgi.chatroomsapi.entity.User;
import com.giorgi.chatroomsapi.enums.Role;
import com.giorgi.chatroomsapi.exception.ForbiddenException;
import com.giorgi.chatroomsapi.exception.ResourceNotFoundException;
import com.giorgi.chatroomsapi.mapper.MessageMapper;
import com.giorgi.chatroomsapi.repository.MessageRepository;
import com.giorgi.chatroomsapi.repository.RoomMembershipRepository;
import com.giorgi.chatroomsapi.repository.RoomRepository;
import com.giorgi.chatroomsapi.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class MessageService {

    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final RoomMembershipRepository roomMembershipRepository;
    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;

    @Transactional
    public MessageResponseDto sendMessage(Long roomId, MessageCreateRequest request) {
        User sender = authenticatedUserProvider.getCurrentUser();
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        if(!roomMembershipRepository.existsByRoomIdAndUserId(roomId, sender.getId())) {
            throw new ForbiddenException("You are not a member of this room");
        }

        Message message = new Message();
        message.setContent(request.getContent());
        message.setSender(sender);
        message.setRoom(room);
        Message saved = messageRepository.save(message);
        return messageMapper.toMessageResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<MessageResponseDto> getMessages(Long roomId, int page, int size,
                                                LocalDateTime fromDate, LocalDateTime toDate) {
        User user = authenticatedUserProvider.getCurrentUser();
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room not found");
        }
        if (!roomMembershipRepository.existsByRoomIdAndUserId(roomId, user.getId())) {
            throw new ForbiddenException("You are not a member of this room");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagesPage = messageRepository.findByRoomIdAndDateRange(roomId, fromDate, toDate, pageable);
        return messagesPage.map(messageMapper::toMessageResponseDto);
    }

    @Transactional
    public MessageResponseDto editMessage(Long roomId, Long messageId, MessageCreateRequest request) {
        User user = authenticatedUserProvider.getCurrentUser();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getRoom().getId().equals(roomId)) {
            throw new ResourceNotFoundException("Message not found in this room");
        }
        if (!message.getSender().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only edit your own messages");
        }
        if (message.isDeleted()) {
            throw new ForbiddenException("Cannot edit a deleted message");
        }

        message.setContent(request.getContent());
        message.setEdited(true);
        Message saved = messageRepository.save(message);
        return messageMapper.toMessageResponseDto(saved);
    }

    @Transactional
    public void deleteMessage(Long roomId, Long messageId) {
        User user = authenticatedUserProvider.getCurrentUser();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getRoom().getId().equals(roomId)) {
            throw new ResourceNotFoundException("Message not found in this room");
        }
        if (message.isDeleted()) {
            throw new ForbiddenException("Message is already deleted");
        }

        boolean isSender = message.getSender().getId().equals(user.getId());
        boolean isModeratorOrAbove = roomMembershipRepository.findByRoomIdAndUserId(roomId, user.getId())
                .map(membership -> membership.getRole() == Role.OWNER ||
                        membership.getRole() == Role.MODERATOR)
                .orElse(false);

        if (!isSender && !isModeratorOrAbove) {
            throw new ForbiddenException("You do not have permission to delete this message");
        }

        message.setDeleted(true);
        messageRepository.save(message);
    }
}
