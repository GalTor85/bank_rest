package com.bankrest.dto;

import com.bankrest.entity.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class CardResponse {
    private Long id;
    private String maskedNumber;
    private String ownerUsername;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal balance;
}