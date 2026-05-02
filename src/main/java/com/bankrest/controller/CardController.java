package com.bankrest.controller;

import com.bankrest.dto.CardRequest;
import com.bankrest.dto.CardResponse;
import com.bankrest.dto.TransferRequest;
import com.bankrest.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    // ===== Admin only =====

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cardService.createCard(request, userDetails.getUsername()));
    }

    @PutMapping("/{cardId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> activateCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.activateCard(cardId));
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok(Map.of("message", "Карта удалена"));
    }

    // ===== User + Admin =====

    @GetMapping("/{cardId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CardResponse> getCard(@PathVariable Long cardId,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cardService.getCard(cardId, userDetails.getUsername()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<CardResponse>> getUserCards(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long ownerId,
            Pageable pageable) {
        return ResponseEntity.ok(cardService.getUserCards(userDetails.getUsername(), ownerId, pageable));
    }

    @PutMapping("/{cardId}/block")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CardResponse> blockCard(@PathVariable Long cardId,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cardService.blockCard(cardId, userDetails.getUsername()));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> transfer(@Valid @RequestBody TransferRequest request,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        cardService.transfer(request, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Перевод выполнен"));
    }
}