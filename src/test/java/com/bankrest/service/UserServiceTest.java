package com.bankrest.service;

import com.bankrest.dto.AuthRequest;
import com.bankrest.dto.AuthResponse;
import com.bankrest.dto.RegisterRequest;
import com.bankrest.entity.Role;
import com.bankrest.entity.User;
import com.bankrest.exception.UserAlreadyExistsException;
import com.bankrest.repository.UserRepository;
import com.bankrest.security.JwtProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtProvider jwtProvider;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    @Test
    void register_ShouldSaveUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest("newuser", "password123", Role.USER);
        User savedUser = User.builder()
                .id(1L)
                .username("newuser")
                .password("hashed")
                .role(Role.USER)
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtProvider.generateToken("newuser", "USER")).thenReturn("token123");

        AuthResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("token123", response.getToken());
        assertEquals("newuser", response.getUsername());
        assertEquals("USER", response.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenUserAlreadyExists() {
        RegisterRequest request = new RegisterRequest("existing", "pass", Role.USER);
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.register(request));
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsValid() {
        AuthRequest request = new AuthRequest("user", "pass");
        User user = User.builder().username("user").password("hashed").role(Role.USER).build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(jwtProvider.generateToken("user", "USER")).thenReturn("token");

        AuthResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals("token", response.getToken());
        verify(authenticationManager).authenticate(any());
    }
}