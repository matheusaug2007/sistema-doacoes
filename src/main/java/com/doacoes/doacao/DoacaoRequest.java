package com.doacoes.doacao;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @param dataRecebimento se omitida, vale a data de hoje
 * @param itens RN02: pelo menos um alimento
 */
public record DoacaoRequest(
        @NotNull Long doadorId,
        LocalDate dataRecebimento,
        @Size(max = 500) String observacoes,
        @NotEmpty List<@Valid Item> itens) {

    public record Item(
            @NotNull Long alimentoId,
            @NotNull @Positive @Digits(integer = 9, fraction = 3) BigDecimal quantidade,
            LocalDate dataValidade) {
    }
}
