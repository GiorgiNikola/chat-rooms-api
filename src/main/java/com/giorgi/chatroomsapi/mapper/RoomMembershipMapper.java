package com.giorgi.chatroomsapi.mapper;

import com.giorgi.chatroomsapi.dto.roommembership.RoomMembershipResponseDto;
import com.giorgi.chatroomsapi.entity.RoomMembership;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMembershipMapper {
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "user.id", target = "userId")
    RoomMembershipResponseDto toRoomMembershipResponseDto(RoomMembership roomMembership);
}
