package com.doacoes.cadastro.doador;

public record DoadorResponse(
        Long id, String nome, TipoDoador tipo, String documento, String telefone, String email, String endereco,
        String observacoes, boolean ativo) {

    public static DoadorResponse de(Doador d) {
        return new DoadorResponse(d.getId(), d.getNome(), d.getTipo(), d.getDocumento(), d.getTelefone(),
                d.getEmail(), d.getEndereco(), d.getObservacoes(), d.isAtivo());
    }
}
