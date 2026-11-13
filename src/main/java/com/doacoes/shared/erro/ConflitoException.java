package com.doacoes.shared.erro;

import java.util.Map;

/** O pedido não pode ser atendido no estado atual dos dados. Vira HTTP 409. */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }

    /** Dados extras para o corpo da resposta de erro. */
    public Map<String, Object> detalhes() {
        return Map.of();
    }
}
