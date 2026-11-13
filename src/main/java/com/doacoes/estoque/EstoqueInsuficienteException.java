package com.doacoes.estoque;

import com.doacoes.shared.Quantidades;
import com.doacoes.shared.erro.ConflitoException;
import java.math.BigDecimal;
import java.util.Map;

/** RN03: tentativa de retirar mais do que há disponível. */
public class EstoqueInsuficienteException extends ConflitoException {

    private final Long alimentoId;
    private final BigDecimal solicitado;
    private final BigDecimal disponivel;

    public EstoqueInsuficienteException(Long alimentoId, String alimento, BigDecimal solicitado,
            BigDecimal disponivel) {
        super("Estoque insuficiente de " + alimento + ": solicitado " + Quantidades.formatar(solicitado)
                + ", disponível " + Quantidades.formatar(disponivel) + ".");
        this.alimentoId = alimentoId;
        this.solicitado = solicitado;
        this.disponivel = disponivel;
    }

    @Override
    public Map<String, Object> detalhes() {
        return Map.of("alimentoId", alimentoId, "solicitado", solicitado, "disponivel", disponivel);
    }

    public BigDecimal getDisponivel() {
        return disponivel;
    }
}
