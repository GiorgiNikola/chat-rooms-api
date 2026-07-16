package com.giorgi.chatroomsapi.mapper;

import com.giorgi.chatroomsapi.dto.user.UserResponseDto;
import com.giorgi.chatroomsapi.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto toUserResponseDto(User user);
}
