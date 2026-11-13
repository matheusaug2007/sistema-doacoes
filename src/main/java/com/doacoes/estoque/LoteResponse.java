package com.doacoes.estoque;

import com.doacoes.cadastro.alimento.UnidadeMedida;
import com.doacoes.doacao.ItemDoacao;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LoteResponse(
        Long loteId, Long doacaoId, LocalDate dataRecebimento, Long alimentoId, String alimento,
        UnidadeMedida unidadeMedida, BigDecimal quantidade, BigDecimal quantidadeDisponivel,
        LocalDate dataValidade, boolean vencido) {

    static LoteResponse de(ItemDoacao lote, LocalDate hoje) {
        return new LoteResponse(lote.getId(), lote.getDoacao().getId(), lote.getDoacao().getDataRecebimento(),
                lote.getAlimento().getId(), lote.getAlimento().getNome(), lote.getAlimento().getUnidadeMedida(),
                lote.getQuantidade(), lote.getQuantidadeDisponivel(), lote.getDataValidade(),
                lote.estaVencidoEm(hoje));
    }
}
