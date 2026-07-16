package com.giorgi.chatroomsapi.service;

import com.giorgi.chatroomsapi.dto.user.AuthResponseDto;
import com.giorgi.chatroomsapi.dto.user.LoginRequest;
import com.giorgi.chatroomsapi.dto.user.UserRegisterRequest;
import com.giorgi.chatroomsapi.dto.user.UserResponseDto;
import com.giorgi.chatroomsapi.entity.User;
import com.giorgi.chatroomsapi.exception.DuplicateResourceException;
import com.giorgi.chatroomsapi.exception.InvalidCredentialsException;
import com.giorgi.chatroomsapi.mapper.UserMapper;
import com.giorgi.chatroomsapi.repository.UserRepository;
import com.giorgi.chatroomsapi.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserResponseDto register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email " + request.getEmail() + " already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());

        User saved = userRepository.save(user);
        return userMapper.toUserResponseDto(saved);
    }

    public AuthResponseDto login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        AuthResponseDto authResponseDto =  new AuthResponseDto();
        authResponseDto.setToken(token);
        return authResponseDto;
    }
}
