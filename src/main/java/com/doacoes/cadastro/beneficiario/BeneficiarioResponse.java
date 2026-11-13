package com.doacoes.cadastro.beneficiario;

public record BeneficiarioResponse(
        Long id, String nome, TipoBeneficiario tipo, String documento, String telefone, String email,
        String endereco, int quantidadePessoas, String observacoes, boolean ativo) {

    public static BeneficiarioResponse de(Beneficiario b) {
        return new BeneficiarioResponse(b.getId(), b.getNome(), b.getTipo(), b.getDocumento(), b.getTelefone(),
                b.getEmail(), b.getEndereco(), b.getQuantidadePessoas(), b.getObservacoes(), b.isAtivo());
    }
}
