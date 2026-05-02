package com.bankrest.service;

import com.bankrest.dto.AuthRequest;
import com.bankrest.dto.AuthResponse;
import com.bankrest.dto.RegisterRequest;
import com.bankrest.entity.User;
import com.bankrest.exception.UserAlreadyExistsException;
import com.bankrest.exception.UserNotFoundException;
import com.bankrest.repository.UserRepository;
import com.bankrest.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException(
                    "Пользователь с именем '" + request.getUsername() + "' уже существует"
            );
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepository.save(user);
        log.info("User registered: {}", user.getUsername());

        String token = jwtProvider.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new com.bankrest.exception.BadCredentialsException("Неверное имя пользователя или пароль");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        log.info("User logged in: {}", user.getUsername());

        String token = jwtProvider.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
}