package com.giorgi.chatroomsapi.websocket;

import com.giorgi.chatroomsapi.dto.message.MessageCreateRequest;
import com.giorgi.chatroomsapi.dto.message.MessageResponseDto;
import com.giorgi.chatroomsapi.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class WebSocketMessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/rooms/{roomId}/send")
    public void sendMessage(@DestinationVariable Long roomId,
                            @Payload MessageCreateRequest request,
                            Principal principal) {
        MessageResponseDto saved = messageService.sendMessage(roomId, request, principal.getName());
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId, saved);
    }
}