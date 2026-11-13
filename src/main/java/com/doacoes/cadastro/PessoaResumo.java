package com.doacoes.cadastro;

/**
 * @param id identificador dentro do cadastro indicado em tipoCadastro
 * @param dados descrição montada pela própria pessoa, conforme o que ela é
 */
public record PessoaResumo(
        TipoCadastro tipoCadastro, Long id, String nome, String telefone, boolean ativo, String dados) {

    static PessoaResumo de(Pessoa pessoa) {
        return new PessoaResumo(pessoa.getTipoCadastro(), pessoa.getId(), pessoa.getNome(), pessoa.getTelefone(),
                pessoa.isAtivo(), pessoa.getDados());
    }
}
