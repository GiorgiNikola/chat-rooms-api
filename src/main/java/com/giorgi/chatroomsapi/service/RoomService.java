package com.giorgi.chatroomsapi.service;

import com.giorgi.chatroomsapi.dto.room.RoomCreateRequest;
import com.giorgi.chatroomsapi.dto.room.RoomResponseDto;
import com.giorgi.chatroomsapi.entity.Room;
import com.giorgi.chatroomsapi.exception.ResourceNotFoundException;
import com.giorgi.chatroomsapi.mapper.RoomMapper;
import com.giorgi.chatroomsapi.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RoomService {

    private final RoomMapper roomMapper;
    private final RoomRepository roomRepository;

    @Transactional
    public RoomResponseDto create(RoomCreateRequest request) {
        Room room = roomMapper.toRoom(request);
        Room saved = roomRepository.save(room);
        return roomMapper.toRoomResponse(saved);
    }

    @Transactional(readOnly = true)
    public RoomResponseDto getById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        return roomMapper.toRoomResponse(room);
    }

    @Transactional(readOnly = true)
    public Page<RoomResponseDto> listRooms(Pageable pageable) {
        return roomRepository.findAll(pageable)
                .map(roomMapper::toRoomResponse);
    }

    @Transactional
    public RoomResponseDto updateRoom(Long roomId, RoomCreateRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        room.setName(request.getName());
        room.setDescription(request.getDescription());
        room.setPrivate(request.isPrivate());
        return roomMapper.toRoomResponse(room);
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        roomRepository.delete(room);
    }
}
