package com.giorgi.chatroomsapi.dto.room;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RoomResponseDto {

    private Long id;

    private String name;

    private String description;

    private boolean isPrivate;

    private Long ownerId;

    private String ownerDisplayName;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;
}
