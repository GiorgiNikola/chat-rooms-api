package com.giorgi.chatroomsapi.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageCreateRequest {

    @NotBlank
    @Size(min = 1, max = 500, message = "Message content must be between 1 and 500 characters")
    private String content;
}
