package com.doacoes.cadastro.instituicao;

import org.springframework.stereotype.Component;

/**
 * Diz a qual instituição pertence a operação em andamento. Todo cadastro e lançamento é
 * gravado e consultado por instituição.
 *
 * Enquanto não há login (Fase 2), existe uma única instituição, criada pela migração V2.
 * Quando houver autenticação, basta este componente passar a ler a instituição do usuário
 * logado; o restante do sistema não muda.
 */
@Component
public class InstituicaoAtual {

    private final InstituicaoRepository repository;
    private volatile Long id;

    public InstituicaoAtual(InstituicaoRepository repository) {
        this.repository = repository;
    }

    public Long id() {
        if (id == null) {
            id = repository.findFirstByOrderByIdAsc()
                    .map(Instituicao::getId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Nenhuma instituição cadastrada; a migração V2 deveria ter criado a padrão."));
        }
        return id;
    }

    /** Referência para usar como chave estrangeira, sem carregar a instituição do banco. */
    public Instituicao referencia() {
        return repository.getReferenceById(id());
    }
}
