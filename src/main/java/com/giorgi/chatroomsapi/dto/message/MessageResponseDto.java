package com.giorgi.chatroomsapi.dto.message;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MessageResponseDto {

    private Long id;

    private Long roomId;

    private Long senderId;

    private String senderDisplayName;

    private String content;

    private LocalDateTime sentAt;

    private boolean edited;

    private boolean deleted;
}
