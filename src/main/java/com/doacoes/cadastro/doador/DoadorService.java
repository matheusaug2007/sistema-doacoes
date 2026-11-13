package com.doacoes.cadastro.doador;

import com.doacoes.cadastro.instituicao.InstituicaoAtual;
import com.doacoes.shared.Pagina;
import com.doacoes.shared.Textos;
import com.doacoes.shared.erro.ConflitoException;
import com.doacoes.shared.erro.RecursoNaoEncontradoException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoadorService {

    private final DoadorRepository repository;
    private final InstituicaoAtual instituicaoAtual;

    public DoadorService(DoadorRepository repository, InstituicaoAtual instituicaoAtual) {
        this.repository = repository;
        this.instituicaoAtual = instituicaoAtual;
    }

    @Transactional(readOnly = true)
    public Pagina<DoadorResponse> listar(Boolean ativo, String busca, Pageable pageable) {
        return Pagina.de(
                repository.buscar(instituicaoAtual.id(), ativo, Textos.padraoDeBusca(busca), pageable),
                DoadorResponse::de);
    }

    @Transactional(readOnly = true)
    public DoadorResponse detalhar(Long id) {
        return DoadorResponse.de(obter(id));
    }

    public DoadorResponse criar(DoadorRequest dados) {
        Doador doador = new Doador(instituicaoAtual.referencia());
        aplicar(doador, dados);
        return DoadorResponse.de(repository.save(doador));
    }

    public DoadorResponse atualizar(Long id, DoadorRequest dados) {
        Doador doador = obter(id);
        aplicar(doador, dados);
        return DoadorResponse.de(doador);
    }

    public void desativar(Long id) {
        obter(id).setAtivo(false);
    }

    public DoadorResponse reativar(Long id) {
        Doador doador = obter(id);
        doador.setAtivo(true);
        return DoadorResponse.de(doador);
    }

    /** Para os outros módulos: devolve a entidade ou falha com 404. */
    @Transactional(readOnly = true)
    public Doador obter(Long id) {
        return repository.findByIdAndInstituicaoId(id, instituicaoAtual.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Doador não encontrado: id " + id));
    }

    private void aplicar(Doador doador, DoadorRequest dados) {
        String documento = Textos.limpar(dados.documento());
        if (documento != null
                && repository.existeOutroComDocumento(instituicaoAtual.id(), documento, doador.getId())) {
            throw new ConflitoException("Já existe um doador com o documento " + documento + ".");
        }
        doador.setNome(dados.nome().strip());
        doador.setTipo(dados.tipo());
        doador.setDocumento(documento);
        doador.setTelefone(Textos.limpar(dados.telefone()));
        doador.setEmail(Textos.limpar(dados.email()));
        doador.setEndereco(Textos.limpar(dados.endereco()));
        doador.setObservacoes(Textos.limpar(dados.observacoes()));
    }
}
