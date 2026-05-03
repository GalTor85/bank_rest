package com.bankrest.controller;

import com.bankrest.dto.CardRequest;
import com.bankrest.dto.CardResponse;
import com.bankrest.dto.TransferRequest;
import com.bankrest.entity.CardStatus;
import com.bankrest.service.CardService;
import com.bankrest.security.JwtProvider;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private JwtProvider jwtProvider;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createCard_ShouldReturn201_WhenAdmin() throws Exception {
        CardRequest request = new CardRequest();
        request.setCardNumber("1234567812345678");
        request.setExpiryDate(LocalDate.now().plusYears(1));
        CardResponse response = CardResponse.builder()
                .id(1L)
                .maskedNumber("**** **** **** 5678")
                .ownerUsername("admin")
                .expiryDate(LocalDate.now().plusYears(1))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        when(cardService.createCard(any(), eq("admin"))).thenReturn(response);

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedNumber").value("**** **** **** 5678"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getCard_ShouldReturnCard_WhenOwner() throws Exception {
        CardResponse response = CardResponse.builder()
                .id(1L)
                .maskedNumber("**** **** **** 1234")
                .ownerUsername("user")
                .expiryDate(LocalDate.now().plusYears(1))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(500))
                .build();

        when(cardService.getCard(1L, "user")).thenReturn(response);

        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getUserCards_ShouldReturnPage() throws Exception {
        CardResponse card = CardResponse.builder()
                .id(1L)
                .maskedNumber("**** **** **** 1234")
                .ownerUsername("user")
                .build();

        when(cardService.getUserCards(eq("user"), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(card)));

        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void blockCard_ShouldReturnBlockedCard() throws Exception {
        CardResponse response = CardResponse.builder()
                .id(1L)
                .status(CardStatus.BLOCKED)
                .build();

        when(cardService.blockCard(1L, "user")).thenReturn(response);

        mockMvc.perform(put("/api/cards/1/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteCard_ShouldReturnMessage() throws Exception {
        mockMvc.perform(delete("/api/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Карта удалена"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void transfer_ShouldReturnSuccessMessage() throws Exception {
        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Перевод выполнен"));
    }
}