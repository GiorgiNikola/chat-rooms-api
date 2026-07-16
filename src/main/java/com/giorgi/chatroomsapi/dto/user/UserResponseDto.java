package com.giorgi.chatroomsapi.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDto {

    private Long id;

    private String email;

    private String displayName;
}
