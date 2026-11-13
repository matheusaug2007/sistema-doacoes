package com.doacoes.estoque;

import java.math.BigDecimal;

public enum TipoMovimentacao {
    /** Lote recebido em uma doação. */
    ENTRADA(1),
    /** Entrega a um beneficiário. */
    SAIDA(-1),
    /** Baixa por vencimento, avaria ou perda. */
    DESCARTE(-1),
    /** Doação cancelada: o lote sai do estoque. */
    ESTORNO_ENTRADA(-1),
    /** Distribuição cancelada: a quantidade volta ao lote. */
    ESTORNO_SAIDA(1);

    private final int sinal;

    TipoMovimentacao(int sinal) {
        this.sinal = sinal;
    }

    /** A quantidade é sempre gravada positiva; o tipo diz se ela soma ou subtrai do saldo. */
    public BigDecimal aplicarSinal(BigDecimal quantidade) {
        return sinal > 0 ? quantidade : quantidade.negate();
    }
}
