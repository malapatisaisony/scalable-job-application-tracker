package com.saisony.jobtracker.user.service;

import com.saisony.jobtracker.exception.ConflictException;
import com.saisony.jobtracker.exception.InvalidCredentialsException;
import com.saisony.jobtracker.security.JwtService;
import com.saisony.jobtracker.security.RefreshTokenService;
import com.saisony.jobtracker.user.dto.LoginRequest;
import com.saisony.jobtracker.user.dto.LoginResponse;
import com.saisony.jobtracker.user.dto.RefreshTokenRequest;
import com.saisony.jobtracker.user.dto.RegisterRequest;
import com.saisony.jobtracker.user.dto.UserResponse;
import com.saisony.jobtracker.user.entity.User;
import com.saisony.jobtracker.user.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException(
                    "Email is already registered"
            );
        }

        User user = new User();

        user.setEmail(request.getEmail());
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );
        user.setRole("USER");

        User savedUser =
                userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getCreatedAt()
        );
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or password"
                        )
                );

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPasswordHash()
                );

        if (!passwordMatches) {
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        String accessToken =
                jwtService.generateToken(user);

        String refreshToken =
                refreshTokenService.createRefreshToken(user);

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getExpirationSeconds()
        );
    }

    @Transactional
    public LoginResponse refreshToken(
            RefreshTokenRequest request) {

        User user =
                refreshTokenService.validateRefreshToken(
                        request.getRefreshToken()
                );

        String newAccessToken =
                jwtService.generateToken(user);

        return new LoginResponse(
                newAccessToken,
                request.getRefreshToken(),
                "Bearer",
                jwtService.getExpirationSeconds()
        );
    }

    public void logout(
            RefreshTokenRequest request) {

        refreshTokenService.revokeRefreshToken(
                request.getRefreshToken()
        );
    }
}