package com.giorgi.chatroomsapi.controller;

import com.giorgi.chatroomsapi.dto.message.MessageCreateRequest;
import com.giorgi.chatroomsapi.dto.message.MessageResponseDto;
import com.giorgi.chatroomsapi.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/rooms/{roomId}/messages")
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponseDto> sendMessage(
            @PathVariable Long roomId,
            @RequestBody @Valid MessageCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(messageService.sendMessage(roomId, request));
    }

    @GetMapping
    public ResponseEntity<Page<MessageResponseDto>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return ResponseEntity.ok(messageService.getMessages(roomId, page, size, fromDate, toDate));
    }

    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageResponseDto> editMessage(
            @PathVariable Long roomId,
            @PathVariable Long messageId,
            @RequestBody @Valid MessageCreateRequest request) {
        return ResponseEntity.ok(messageService.editMessage(roomId, messageId, request));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long roomId, @PathVariable Long messageId) {
        messageService.deleteMessage(roomId, messageId);
        return ResponseEntity.noContent().build();
    }
}
