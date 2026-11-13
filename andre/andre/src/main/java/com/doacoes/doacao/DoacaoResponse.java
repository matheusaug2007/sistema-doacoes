package com.doacoes.doacao;

import com.doacoes.cadastro.alimento.UnidadeMedida;
import com.doacoes.shared.Referencia;
import com.doacoes.shared.StatusLancamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DoacaoResponse(
        Long id, LocalDate dataRecebimento, StatusLancamento status, Referencia doador, String observacoes,
        String motivoCancelamento, LocalDateTime criadoEm, LocalDateTime canceladoEm, List<Item> itens) {

    /** Cada item é um lote; loteId é o identificador usado nas consultas de estoque. */
    public record Item(
            Long loteId, Long alimentoId, String alimento, UnidadeMedida unidadeMedida, BigDecimal quantidade,
            BigDecimal quantidadeDisponivel, LocalDate dataValidade) {
    }

    public static DoacaoResponse de(Doacao d) {
        List<Item> itens = d.getItens().stream()
                .map(i -> new Item(i.getId(), i.getAlimento().getId(), i.getAlimento().getNome(),
                        i.getAlimento().getUnidadeMedida(), i.getQuantidade(), i.getQuantidadeDisponivel(),
                        i.getDataValidade()))
                .toList();
        return new DoacaoResponse(d.getId(), d.getDataRecebimento(), d.getStatus(),
                new Referencia(d.getDoador().getId(), d.getDoador().getNome()), d.getObservacoes(),
                d.getMotivoCancelamento(), d.getCriadoEm(), d.getCanceladoEm(), itens);
    }
}
