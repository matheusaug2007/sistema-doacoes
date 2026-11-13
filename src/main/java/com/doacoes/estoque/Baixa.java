package com.doacoes.estoque;

import com.doacoes.doacao.ItemDoacao;
import java.math.BigDecimal;

/** Quanto saiu de qual lote em uma baixa de estoque. */
public record Baixa(ItemDoacao lote, BigDecimal quantidade) {
}
