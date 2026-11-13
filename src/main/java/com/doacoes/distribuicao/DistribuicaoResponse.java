package com.doacoes.distribuicao;

import com.doacoes.cadastro.alimento.Alimento;
import com.doacoes.cadastro.alimento.UnidadeMedida;
import com.doacoes.shared.Referencia;
import com.doacoes.shared.StatusLancamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DistribuicaoResponse(
        Long id, LocalDate dataDistribuicao, StatusLancamento status, Referencia beneficiario, String observacoes,
        String motivoCancelamento, LocalDateTime criadoEm, LocalDateTime canceladoEm, List<Item> itens) {

    /** Uma linha por lote usado: um pedido de um alimento pode ter saído de vários lotes. */
    public record Item(
            Long loteId, Long alimentoId, String alimento, UnidadeMedida unidadeMedida, BigDecimal quantidade,
            LocalDate dataValidade) {
    }

    public static DistribuicaoResponse de(Distribuicao d) {
        List<Item> itens = d.getItens().stream()
                .map(i -> {
                    Alimento alimento = i.getLote().getAlimento();
                    return new Item(i.getLote().getId(), alimento.getId(), alimento.getNome(),
                            alimento.getUnidadeMedida(), i.getQuantidade(), i.getLote().getDataValidade());
                })
                .toList();
        return new DistribuicaoResponse(d.getId(), d.getDataDistribuicao(), d.getStatus(),
                new Referencia(d.getBeneficiario().getId(), d.getBeneficiario().getNome()), d.getObservacoes(),
                d.getMotivoCancelamento(), d.getCriadoEm(), d.getCanceladoEm(), itens);
    }
}
