package com.doacoes.estoque;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * @param quantidade se omitida, descarta todo o saldo do lote
 */
public record DescarteRequest(
        @Positive @Digits(integer = 9, fraction = 3) BigDecimal quantidade,
        @NotBlank @Size(max = 255) String motivo) {
}
