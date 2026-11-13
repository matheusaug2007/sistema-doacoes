package com.doacoes.cadastro.instituicao;

public record InstituicaoResponse(
        Long id, String nome, String cnpj, String telefone, String email, String endereco, String responsavel) {

    public static InstituicaoResponse de(Instituicao i) {
        return new InstituicaoResponse(i.getId(), i.getNome(), i.getCnpj(), i.getTelefone(), i.getEmail(),
                i.getEndereco(), i.getResponsavel());
    }
}
