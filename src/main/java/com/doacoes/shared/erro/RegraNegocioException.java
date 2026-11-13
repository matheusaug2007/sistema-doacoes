package com.doacoes.shared.erro;

/** O pedido é bem formado, mas contraria uma regra de negócio. Vira HTTP 422. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
