package com.bankrest.controller;

import com.bankrest.dto.UserResponse;
import com.bankrest.dto.UpdateUserRequest;
import com.bankrest.entity.Role;
import com.bankrest.security.JwtProvider;
import com.bankrest.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtProvider jwtProvider;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllUsers_ShouldReturnPage() throws Exception {
        UserResponse user1 = UserResponse.builder()
                .id(1L).username("admin").role(Role.ADMIN).build();
        UserResponse user2 = UserResponse.builder()
                .id(2L).username("user").role(Role.USER).build();

        when(userService.getAllUsers(any()))
                .thenReturn(new PageImpl<>(List.of(user1, user2)));

        mockMvc.perform(get("/api/admin/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].username").value("user"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getUserById_ShouldReturnUser() throws Exception {
        UserResponse user = UserResponse.builder()
                .id(1L).username("admin").role(Role.ADMIN).build();
        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest(Role.USER);
        UserResponse response = UserResponse.builder()
                .id(1L).username("admin").role(Role.USER).build();
        when(userService.updateUser(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteUser_ShouldReturnMessage() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Пользователь удалён"));
    }
}