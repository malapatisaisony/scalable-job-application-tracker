package com.saisony.jobtracker.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.LinkedHashMap;
import java.util.Map;

import com.saisony.jobtracker.user.dto.LoginRequest;
import com.saisony.jobtracker.user.dto.LoginResponse;
import com.saisony.jobtracker.user.dto.RegisterRequest;
import com.saisony.jobtracker.user.dto.UserResponse;
import com.saisony.jobtracker.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.saisony.jobtracker.user.dto.RefreshTokenRequest;
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse response = userService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = userService.login(request);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal Jwt jwt) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("userId", jwt.getClaim("userId"));
        response.put("email", jwt.getSubject());
        response.put("role", jwt.getClaim("role"));

        return ResponseEntity.ok(response);
    }
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        LoginResponse response = userService.refreshToken(request);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request) {

        userService.logout(request);

        return ResponseEntity.noContent().build();
    }
}