package com.bankrest.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotNull(message = "ID карты отправителя обязателен")
    private Long fromCardId;

    @NotNull(message = "ID карты получателя обязателен")
    private Long toCardId;

    @NotNull(message = "Сумма перевода обязательна")
    @DecimalMin(value = "0.01", message = "Сумма перевода должна быть больше нуля")
    private BigDecimal amount;
}