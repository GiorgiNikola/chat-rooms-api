package com.giorgi.chatroomsapi.mapper;

import com.giorgi.chatroomsapi.dto.message.MessageResponseDto;
import com.giorgi.chatroomsapi.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.displayName", target = "senderDisplayName")
    @Mapping(source = "room.id", target = "roomId")
    MessageResponseDto toMessageResponseDto(Message message);
}
