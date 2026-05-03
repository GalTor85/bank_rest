package com.bankrest.service;

import com.bankrest.dto.*;
import com.bankrest.entity.Role;
import com.bankrest.entity.User;
import com.bankrest.exception.UserAlreadyExistsException;
import com.bankrest.exception.UserNotFoundException;
import com.bankrest.repository.UserRepository;
import com.bankrest.security.JwtProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
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

    @Test
    void getAllUsers_ShouldReturnPageOfUsers() {
        User user1 = User.builder().id(1L).username("admin").role(Role.ADMIN).password("pass").build();
        User user2 = User.builder().id(2L).username("user").role(Role.USER).password("pass").build();
        Page<User> userPage = new PageImpl<>(List.of(user1, user2));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        Page<UserResponse> result = userService.getAllUsers(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("admin", result.getContent().get(0).getUsername());
        assertEquals(Role.ADMIN, result.getContent().get(0).getRole());
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        User user = User.builder().id(1L).username("admin").role(Role.ADMIN).password("pass").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("admin", response.getUsername());
        assertEquals(Role.ADMIN, response.getRole());
    }

    @Test
    void getUserById_ShouldThrowException_WhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void updateUser_ShouldChangeRoleAndReturnUpdatedUser() {
        User existingUser = User.builder().id(1L).username("user").role(Role.USER).password("pass").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UpdateUserRequest request = new UpdateUserRequest(Role.ADMIN);
        UserResponse response = userService.updateUser(1L, request);

        assertNotNull(response);
        assertEquals(Role.ADMIN, response.getRole());
        verify(userRepository).save(existingUser);
        assertEquals(Role.ADMIN, existingUser.getRole());
    }

    @Test
    void deleteUser_ShouldCallRepositoryDelete() {
        User user = User.builder().id(1L).username("user").role(Role.USER).password("pass").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(99L));
    }
}