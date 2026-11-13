package com.doacoes.cadastro.instituicao;

import com.doacoes.shared.Textos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InstituicaoService {

    private final InstituicaoRepository repository;
    private final InstituicaoAtual instituicaoAtual;

    public InstituicaoService(InstituicaoRepository repository, InstituicaoAtual instituicaoAtual) {
        this.repository = repository;
        this.instituicaoAtual = instituicaoAtual;
    }

    @Transactional(readOnly = true)
    public InstituicaoResponse detalhar() {
        return InstituicaoResponse.de(atual());
    }

    public InstituicaoResponse atualizar(InstituicaoRequest dados) {
        Instituicao instituicao = atual();
        instituicao.atualizar(dados.nome().strip(), Textos.limpar(dados.cnpj()), Textos.limpar(dados.telefone()),
                Textos.limpar(dados.email()), Textos.limpar(dados.endereco()), Textos.limpar(dados.responsavel()));
        return InstituicaoResponse.de(instituicao);
    }

    private Instituicao atual() {
        return repository.findById(instituicaoAtual.id()).orElseThrow();
    }
}
