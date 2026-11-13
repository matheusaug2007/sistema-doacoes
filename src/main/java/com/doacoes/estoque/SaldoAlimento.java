package com.doacoes.estoque;

import com.doacoes.cadastro.alimento.Alimento;
import com.doacoes.cadastro.alimento.UnidadeMedida;
import com.doacoes.doacao.ItemDoacao;
import com.doacoes.shared.Quantidades;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * @param quantidadeDisponivel soma dos lotes ainda válidos, ou seja, o que pode ser distribuído
 * @param quantidadeVencida soma dos lotes vencidos que ainda aguardam descarte
 * @param proximoVencimento validade mais próxima entre os lotes válidos; null se nenhum tem validade
 * @param lotes quantos lotes válidos compõem o saldo disponível
 */
public record SaldoAlimento(
        Long alimentoId, String alimento, String categoria, UnidadeMedida unidadeMedida,
        BigDecimal quantidadeDisponivel, BigDecimal quantidadeVencida, LocalDate proximoVencimento, int lotes) {

    static SaldoAlimento de(Alimento alimento, List<ItemDoacao> lotesComSaldo, LocalDate hoje) {
        List<ItemDoacao> validos = lotesComSaldo.stream().filter(lote -> !lote.estaVencidoEm(hoje)).toList();
        List<ItemDoacao> vencidos = lotesComSaldo.stream().filter(lote -> lote.estaVencidoEm(hoje)).toList();
        LocalDate proximoVencimento = validos.stream()
                .map(ItemDoacao::getDataValidade)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(null);
        return new SaldoAlimento(alimento.getId(), alimento.getNome(), alimento.getCategoria(),
                alimento.getUnidadeMedida(), somar(validos), somar(vencidos), proximoVencimento, validos.size());
    }

    private static BigDecimal somar(List<ItemDoacao> lotes) {
        return lotes.stream().map(ItemDoacao::getQuantidadeDisponivel).reduce(Quantidades.ZERO, BigDecimal::add);
    }
}
