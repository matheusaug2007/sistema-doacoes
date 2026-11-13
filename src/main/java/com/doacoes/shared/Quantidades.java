package com.doacoes.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Quantidades de alimento têm sempre três casas decimais, como nas colunas DECIMAL(12,3). */
public final class Quantidades {

    public static final BigDecimal ZERO = new BigDecimal("0.000");

    private Quantidades() {
    }

    /** Ajusta para três casas sem arredondar; a API já recusa valores com mais casas que isso. */
    public static BigDecimal normalizar(BigDecimal quantidade) {
        return quantidade.setScale(3, RoundingMode.UNNECESSARY);
    }

    /** Texto sem zeros à direita, para mensagens: 8.000 vira "8". */
    public static String formatar(BigDecimal quantidade) {
        return quantidade.stripTrailingZeros().toPlainString();
    }
}
