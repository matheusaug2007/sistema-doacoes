package com.doacoes.estoque;

import com.doacoes.cadastro.alimento.UnidadeMedida;
import com.doacoes.doacao.ItemDoacao;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @param quantidade sempre positiva
 * @param variacao a mesma quantidade com o sinal do efeito no saldo (negativa nas saídas)
 */
public record MovimentacaoResponse(
        Long id, TipoMovimentacao tipo, BigDecimal quantidade, BigDecimal variacao, Long loteId, Long doacaoId,
        Long distribuicaoId, Long alimentoId, String alimento, UnidadeMedida unidadeMedida, String motivo,
        LocalDateTime criadoEm) {

    static MovimentacaoResponse de(MovimentacaoEstoque m) {
        ItemDoacao lote = m.getLote();
        return new MovimentacaoResponse(m.getId(), m.getTipo(), m.getQuantidade(),
                m.getTipo().aplicarSinal(m.getQuantidade()), lote.getId(), lote.getDoacao().getId(),
                m.getDistribuicaoId(), lote.getAlimento().getId(), lote.getAlimento().getNome(),
                lote.getAlimento().getUnidadeMedida(), m.getMotivo(), m.getCriadoEm());
    }
}
