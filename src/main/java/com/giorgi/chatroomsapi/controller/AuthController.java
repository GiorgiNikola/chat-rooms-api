package com.giorgi.chatroomsapi.controller;

import com.giorgi.chatroomsapi.dto.user.AuthResponseDto;
import com.giorgi.chatroomsapi.dto.user.LoginRequest;
import com.giorgi.chatroomsapi.dto.user.UserRegisterRequest;
import com.giorgi.chatroomsapi.dto.user.UserResponseDto;
import com.giorgi.chatroomsapi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody @Valid UserRegisterRequest request) {
        UserResponseDto userResponse = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid LoginRequest request) {
        AuthResponseDto authResponse = authService.login(request);
        return ResponseEntity.ok(authResponse);
    }
}
