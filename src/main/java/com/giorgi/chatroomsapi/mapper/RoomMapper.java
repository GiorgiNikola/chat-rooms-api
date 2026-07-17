package com.giorgi.chatroomsapi.mapper;

import com.giorgi.chatroomsapi.dto.room.RoomCreateRequest;
import com.giorgi.chatroomsapi.dto.room.RoomResponseDto;
import com.giorgi.chatroomsapi.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "owner.displayName", target = "ownerDisplayName")
    RoomResponseDto toRoomResponse(Room room);

    Room toRoom(RoomCreateRequest request);
}
