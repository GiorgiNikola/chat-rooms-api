package com.giorgi.chatroomsapi.dto.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomCreateRequest {

    @NotBlank()
    @Size(min = 3, max = 30, message = "Room name must be between 3 and 30 characters")
    private String name;

    @NotBlank()
    @Size(min = 10, max = 200, message = "Description must be between 10 and 200 characters")
    private String description;

    private boolean isPrivate;
}
