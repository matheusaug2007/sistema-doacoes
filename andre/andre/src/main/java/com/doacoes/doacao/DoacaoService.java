package com.doacoes.doacao;

import com.doacoes.cadastro.alimento.Alimento;
import com.doacoes.cadastro.alimento.AlimentoService;
import com.doacoes.cadastro.doador.Doador;
import com.doacoes.cadastro.doador.DoadorService;
import com.doacoes.cadastro.instituicao.InstituicaoAtual;
import com.doacoes.estoque.EstoqueService;
import com.doacoes.shared.Pagina;
import com.doacoes.shared.Textos;
import com.doacoes.shared.erro.RecursoNaoEncontradoException;
import com.doacoes.shared.erro.RegraNegocioException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoacaoService {

    private final DoacaoRepository repository;
    private final DoadorService doadorService;
    private final AlimentoService alimentoService;
    private final EstoqueService estoqueService;
    private final InstituicaoAtual instituicaoAtual;
    private final Clock clock;

    public DoacaoService(DoacaoRepository repository, DoadorService doadorService,
            AlimentoService alimentoService, EstoqueService estoqueService, InstituicaoAtual instituicaoAtual,
            Clock clock) {
        this.repository = repository;
        this.doadorService = doadorService;
        this.alimentoService = alimentoService;
        this.estoqueService = estoqueService;
        this.instituicaoAtual = instituicaoAtual;
        this.clock = clock;
    }

    /** Registra a doação e dá entrada de cada item no estoque, tudo na mesma transação (RN05). */
    public DoacaoResponse registrar(DoacaoRequest dados) {
        LocalDate hoje = LocalDate.now(clock);
        LocalDate recebimento = dados.dataRecebimento() != null ? dados.dataRecebimento() : hoje;
        if (recebimento.isAfter(hoje)) {
            throw new RegraNegocioException("A data de recebimento não pode ser futura.");
        }
        Doador doador = doadorService.obter(dados.doadorId());
        if (!doador.isAtivo()) {
            throw new RegraNegocioException("O doador " + doador.getNome() + " está inativo.");
        }

        Doacao doacao = new Doacao(instituicaoAtual.referencia(), doador, recebimento,
                Textos.limpar(dados.observacoes()), LocalDateTime.now(clock));
        for (DoacaoRequest.Item item : dados.itens()) {
            Alimento alimento = alimentoService.obter(item.alimentoId());
            if (!alimento.isAtivo()) {
                throw new RegraNegocioException("O alimento " + alimento.getNome() + " está inativo.");
            }
            doacao.adicionarItem(alimento, item.quantidade(), item.dataValidade());
        }
        repository.save(doacao);
        doacao.getItens().forEach(estoqueService::registrarEntrada);
        return DoacaoResponse.de(doacao);
    }

    /**
     * Cancela a doação e retira os seus lotes do estoque. Só é possível enquanto nada saiu
     * deles; se já houve distribuição ou descarte, o histórico não pode ser desfeito por aqui.
     */
    public DoacaoResponse cancelar(Long id, String motivo) {
        Doacao doacao = obter(id);
        doacao.cancelar(motivo.strip(), LocalDateTime.now(clock));
        estoqueService.estornarEntradas(doacao.getId(), "Cancelamento da doação " + doacao.getId());
        return DoacaoResponse.de(doacao);
    }

    @Transactional(readOnly = true)
    public Pagina<DoacaoResponse> listar(Long doadorId, LocalDate de, LocalDate ate, Pageable pageable) {
        return Pagina.de(repository.buscar(instituicaoAtual.id(), doadorId, de, ate, pageable), DoacaoResponse::de);
    }

    @Transactional(readOnly = true)
    public DoacaoResponse detalhar(Long id) {
        return DoacaoResponse.de(obter(id));
    }

    private Doacao obter(Long id) {
        return repository.findByIdAndInstituicaoId(id, instituicaoAtual.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Doação não encontrada: id " + id));
    }
}
