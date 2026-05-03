package com.bankrest.service;

import com.bankrest.dto.CardRequest;
import com.bankrest.dto.CardResponse;
import com.bankrest.dto.TransferRequest;
import com.bankrest.entity.Card;
import com.bankrest.entity.CardStatus;
import com.bankrest.entity.Role;
import com.bankrest.entity.User;
import com.bankrest.exception.AccessDeniedException;
import com.bankrest.exception.CardNotFoundException;
import com.bankrest.exception.InsufficientFundsException;
import com.bankrest.repository.CardRepository;
import com.bankrest.repository.UserRepository;
import com.bankrest.util.CardMaskUtil;
import com.bankrest.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock private CardRepository cardRepository;
    @Mock private UserRepository userRepository;
    @Mock private EncryptionUtil encryptionUtil;
    @Mock private CardMaskUtil cardMaskUtil;

    @InjectMocks
    private CardService cardService;

    private User admin;
    private User user;
    private Card card1;
    private Card card2;

    @BeforeEach
    void setUp() {
        admin = User.builder()
                .id(1L)
                .username("admin")
                .password("pass")
                .role(Role.ADMIN)
                .build();

        user = User.builder()
                .id(2L)
                .username("user")
                .password("pass")
                .role(Role.USER)
                .build();

        card1 = Card.builder()
                .id(1L)
                .encryptedNumber("encrypted1")
                .lastFourDigits("1234")
                .owner(user)
                .expiryDate(LocalDate.now().plusYears(1))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .build();

        card2 = Card.builder()
                .id(2L)
                .encryptedNumber("encrypted2")
                .lastFourDigits("5678")
                .owner(user)
                .expiryDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(500))
                .build();
    }

    // ===== getCard =====

    @Test
    void getCard_ShouldReturnCard_WhenUserIsOwner() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));
        when(cardMaskUtil.mask("1234")).thenReturn("**** **** **** 1234");

        CardResponse response = cardService.getCard(1L, "user");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("**** **** **** 1234", response.getMaskedNumber());
        verify(cardRepository).findById(1L);
    }

    @Test
    void getCard_ShouldThrowAccessDenied_WhenUserIsNotOwner() {
        User otherUser = User.builder().id(3L).username("other").role(Role.USER).build();
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));

        assertThrows(AccessDeniedException.class, () -> cardService.getCard(1L, "other"));
    }

    @Test
    void getCard_ShouldReturnCard_WhenAdminRequests() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));
        when(cardMaskUtil.mask("1234")).thenReturn("**** **** **** 1234");

        CardResponse response = cardService.getCard(1L, "admin");

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    // ===== createCard =====

    @Test
    void createCard_ShouldSaveCardWithEncryptedNumber() {
        CardRequest request = new CardRequest("1234567812345678", LocalDate.now().plusYears(1));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(encryptionUtil.encrypt("1234567812345678")).thenReturn("encrypted");
        when(cardRepository.save(any(Card.class))).thenReturn(card1);
        when(cardMaskUtil.mask("1234")).thenReturn("**** **** **** 1234");

        CardResponse response = cardService.createCard(request, "user");

        assertNotNull(response);
        verify(encryptionUtil).encrypt("1234567812345678");
        verify(cardRepository).save(any(Card.class));
    }

    // ===== blockCard =====

    @Test
    void blockCard_ShouldSetStatusBlocked_WhenOwnerRequests() {
        Card blockedCard = Card.builder()
                .id(1L)
                .encryptedNumber("encrypted1")
                .lastFourDigits("1234")
                .owner(user)
                .expiryDate(LocalDate.now().plusYears(1))
                .status(CardStatus.BLOCKED)  // already blocked
                .balance(BigDecimal.valueOf(1000))
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));
        when(cardRepository.save(any(Card.class))).thenReturn(blockedCard);
        when(cardMaskUtil.mask("1234")).thenReturn("**** **** **** 1234");

        CardResponse response = cardService.blockCard(1L, "user");

        assertEquals(CardStatus.BLOCKED, response.getStatus());
    }

    @Test
    void blockCard_ShouldThrowAccessDenied_WhenOtherUserRequests() {
        User otherUser = User.builder().id(3L).username("other").role(Role.USER).build();
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));

        assertThrows(AccessDeniedException.class, () -> cardService.blockCard(1L, "other"));
    }

    // ===== transfer =====

    @Test
    void transfer_ShouldTransferMoney_BetweenOwnCards() {
        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(200));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card2));

        cardService.transfer(request, "user");

        assertEquals(BigDecimal.valueOf(800), card1.getBalance());
        assertEquals(BigDecimal.valueOf(700), card2.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transfer_ShouldThrowInsufficientFunds_WhenNotEnoughBalance() {
        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(2000));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card2));

        assertThrows(InsufficientFundsException.class, () -> cardService.transfer(request, "user"));
    }

    @Test
    void transfer_ShouldThrowAccessDenied_WhenCardBelongsToOtherUser() {
        User otherUser = User.builder().id(3L).username("other").role(Role.USER).build();
        card2.setOwner(otherUser);

        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(100));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card2));

        assertThrows(AccessDeniedException.class, () -> cardService.transfer(request, "user"));
    }

    // ===== deleteCard =====

    @Test
    void deleteCard_ShouldDeleteCard_WhenCardExists() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));

        cardService.deleteCard(1L);

        verify(cardRepository).delete(card1);
    }

    @Test
    void deleteCard_ShouldThrowException_WhenCardNotFound() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.deleteCard(99L));
    }
}