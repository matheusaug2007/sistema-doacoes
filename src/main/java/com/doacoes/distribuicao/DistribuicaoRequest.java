package com.doacoes.distribuicao;

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
 * O pedido diz qual alimento e quanto; de quais lotes sai é o sistema que decide (FEFO).
 *
 * @param dataDistribuicao se omitida, vale a data de hoje
 */
public record DistribuicaoRequest(
        @NotNull Long beneficiarioId,
        LocalDate dataDistribuicao,
        @Size(max = 500) String observacoes,
        @NotEmpty List<@Valid Item> itens) {

    public record Item(
            @NotNull Long alimentoId,
            @NotNull @Positive @Digits(integer = 9, fraction = 3) BigDecimal quantidade) {
    }
}
