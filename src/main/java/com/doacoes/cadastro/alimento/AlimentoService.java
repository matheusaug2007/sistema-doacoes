package com.doacoes.cadastro.alimento;

import com.doacoes.cadastro.instituicao.InstituicaoAtual;
import com.doacoes.shared.Pagina;
import com.doacoes.shared.Textos;
import com.doacoes.shared.erro.ConflitoException;
import com.doacoes.shared.erro.RecursoNaoEncontradoException;
import com.doacoes.shared.erro.RegraNegocioException;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AlimentoService {

    private final AlimentoRepository repository;
    private final InstituicaoAtual instituicaoAtual;

    public AlimentoService(AlimentoRepository repository, InstituicaoAtual instituicaoAtual) {
        this.repository = repository;
        this.instituicaoAtual = instituicaoAtual;
    }

    @Transactional(readOnly = true)
    public Pagina<AlimentoResponse> listar(Boolean ativo, String busca, Pageable pageable) {
        return Pagina.de(
                repository.buscar(instituicaoAtual.id(), ativo, Textos.padraoDeBusca(busca), pageable),
                AlimentoResponse::de);
    }

    @Transactional(readOnly = true)
    public AlimentoResponse detalhar(Long id) {
        return AlimentoResponse.de(obter(id));
    }

    public AlimentoResponse criar(AlimentoRequest dados) {
        Alimento alimento = new Alimento(instituicaoAtual.referencia());
        aplicar(alimento, dados);
        return AlimentoResponse.de(repository.save(alimento));
    }

    public AlimentoResponse atualizar(Long id, AlimentoRequest dados) {
        Alimento alimento = obter(id);
        aplicar(alimento, dados);
        return AlimentoResponse.de(alimento);
    }

    public void desativar(Long id) {
        obter(id).setAtivo(false);
    }

    public AlimentoResponse reativar(Long id) {
        Alimento alimento = obter(id);
        alimento.setAtivo(true);
        return AlimentoResponse.de(alimento);
    }

    /** Para os outros módulos: devolve a entidade ou falha com 404. */
    @Transactional(readOnly = true)
    public Alimento obter(Long id) {
        return repository.findByIdAndInstituicaoId(id, instituicaoAtual.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alimento não encontrado: id " + id));
    }

    /** Todos os alimentos da instituição, ativos ou não, em ordem alfabética. */
    @Transactional(readOnly = true)
    public List<Alimento> todos() {
        return repository.findByInstituicaoIdOrderByNome(instituicaoAtual.id());
    }

    private void aplicar(Alimento alimento, AlimentoRequest dados) {
        String nome = dados.nome().strip();
        if (repository.existeOutroComNome(instituicaoAtual.id(), nome.toLowerCase(), alimento.getId())) {
            throw new ConflitoException("Já existe um alimento chamado " + nome + ".");
        }
        // Trocar a unidade depois de haver lotes mudaria o significado das quantidades já gravadas.
        boolean mudouUnidade = alimento.getId() != null && alimento.getUnidadeMedida() != dados.unidadeMedida();
        if (mudouUnidade && repository.possuiLotes(alimento.getId())) {
            throw new RegraNegocioException("A unidade de medida de " + alimento.getNome()
                    + " não pode ser alterada porque já existem doações registradas para este alimento.");
        }
        alimento.setNome(nome);
        alimento.setCategoria(dados.categoria().strip());
        alimento.setUnidadeMedida(dados.unidadeMedida());
    }
}
