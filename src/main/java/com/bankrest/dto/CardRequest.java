package com.bankrest.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardRequest {

    @NotBlank(message = "Номер карты обязателен")
    @Size(min = 16, max = 16, message = "Номер карты должен содержать 16 цифр")
    private String cardNumber;

    @NotNull(message = "Срок действия обязателен")
    @Future(message = "Срок действия должен быть в будущем")
    private LocalDate expiryDate;
}