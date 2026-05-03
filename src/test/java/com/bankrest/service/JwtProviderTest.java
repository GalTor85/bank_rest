package com.bankrest.service;

import com.bankrest.security.JwtProperties;
import com.bankrest.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437");
        props.setExpiration(86400000L);
        jwtProvider = new JwtProvider(props);
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String token = jwtProvider.generateToken("admin", "ADMIN");

        assertNotNull(token);
        assertTrue(jwtProvider.validateToken(token));
    }

    @Test
    void getUsername_ShouldReturnCorrectUsername() {
        String token = jwtProvider.generateToken("user", "USER");

        assertEquals("user", jwtProvider.getUsername(token));
    }

    @Test
    void getRole_ShouldReturnCorrectRole() {
        String token = jwtProvider.generateToken("admin", "ADMIN");

        assertEquals("ADMIN", jwtProvider.getRole(token));
    }

    @Test
    void validateToken_ShouldReturnFalse_ForInvalidToken() {
        assertFalse(jwtProvider.validateToken("invalid.token.string"));
    }

    @Test
    void validateToken_ShouldReturnFalse_ForEmptyToken() {
        assertFalse(jwtProvider.validateToken(""));
    }
}