package com.doacoes.cadastro.alimento;

public record AlimentoResponse(Long id, String nome, String categoria, UnidadeMedida unidadeMedida, boolean ativo) {

    public static AlimentoResponse de(Alimento a) {
        return new AlimentoResponse(a.getId(), a.getNome(), a.getCategoria(), a.getUnidadeMedida(), a.isAtivo());
    }
}
