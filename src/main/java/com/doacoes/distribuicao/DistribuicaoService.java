package com.doacoes.distribuicao;

import com.doacoes.cadastro.alimento.Alimento;
import com.doacoes.cadastro.alimento.AlimentoService;
import com.doacoes.cadastro.beneficiario.Beneficiario;
import com.doacoes.cadastro.beneficiario.BeneficiarioService;
import com.doacoes.cadastro.instituicao.InstituicaoAtual;
import com.doacoes.estoque.Baixa;
import com.doacoes.estoque.EstoqueService;
import com.doacoes.shared.Pagina;
import com.doacoes.shared.Textos;
import com.doacoes.shared.erro.RecursoNaoEncontradoException;
import com.doacoes.shared.erro.RegraNegocioException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DistribuicaoService {

    private final DistribuicaoRepository repository;
    private final BeneficiarioService beneficiarioService;
    private final AlimentoService alimentoService;
    private final EstoqueService estoqueService;
    private final InstituicaoAtual instituicaoAtual;
    private final Clock clock;

    public DistribuicaoService(DistribuicaoRepository repository, BeneficiarioService beneficiarioService,
            AlimentoService alimentoService, EstoqueService estoqueService, InstituicaoAtual instituicaoAtual,
            Clock clock) {
        this.repository = repository;
        this.beneficiarioService = beneficiarioService;
        this.alimentoService = alimentoService;
        this.estoqueService = estoqueService;
        this.instituicaoAtual = instituicaoAtual;
        this.clock = clock;
    }

    /**
     * Registra a entrega e baixa o estoque na mesma transação: se faltar qualquer alimento,
     * nada é gravado (RN03, RN05).
     */
    public DistribuicaoResponse registrar(DistribuicaoRequest dados) {
        LocalDate hoje = LocalDate.now(clock);
        LocalDate data = dados.dataDistribuicao() != null ? dados.dataDistribuicao() : hoje;
        if (data.isAfter(hoje)) {
            throw new RegraNegocioException("A data da distribuição não pode ser futura.");
        }
        Beneficiario beneficiario = beneficiarioService.obter(dados.beneficiarioId());
        if (!beneficiario.isAtivo()) {
            throw new RegraNegocioException("O beneficiário " + beneficiario.getNome() + " está inativo.");
        }

        Distribuicao distribuicao = repository.save(new Distribuicao(instituicaoAtual.referencia(), beneficiario,
                data, Textos.limpar(dados.observacoes()), LocalDateTime.now(clock)));

        // Soma pedidos repetidos do mesmo alimento. O TreeMap faz os lotes serem travados
        // sempre em ordem de alimento, o que evita deadlock entre distribuições simultâneas.
        Map<Long, BigDecimal> pedidoPorAlimento = new TreeMap<>();
        for (DistribuicaoRequest.Item item : dados.itens()) {
            pedidoPorAlimento.merge(item.alimentoId(), item.quantidade(), BigDecimal::add);
        }
        pedidoPorAlimento.forEach((alimentoId, quantidade) -> {
            Alimento alimento = alimentoService.obter(alimentoId);
            for (Baixa baixa : estoqueService.baixarPorFefo(alimento, quantidade, data, distribuicao.getId())) {
                distribuicao.adicionarItem(baixa.lote(), baixa.quantidade());
            }
        });
        return DistribuicaoResponse.de(distribuicao);
    }

    /** Cancela a distribuição e devolve cada quantidade ao lote de onde saiu. */
    public DistribuicaoResponse cancelar(Long id, String motivo) {
        Distribuicao distribuicao = obter(id);
        distribuicao.cancelar(motivo.strip(), LocalDateTime.now(clock));
        String motivoEstorno = "Cancelamento da distribuição " + distribuicao.getId();
        for (ItemDistribuicao item : distribuicao.getItens()) {
            estoqueService.estornarSaida(item.getLote().getId(), item.getQuantidade(), distribuicao.getId(),
                    motivoEstorno);
        }
        return DistribuicaoResponse.de(distribuicao);
    }

    @Transactional(readOnly = true)
    public Pagina<DistribuicaoResponse> listar(Long beneficiarioId, LocalDate de, LocalDate ate,
            Pageable pageable) {
        return Pagina.de(repository.buscar(instituicaoAtual.id(), beneficiarioId, de, ate, pageable),
                DistribuicaoResponse::de);
    }

    @Transactional(readOnly = true)
    public DistribuicaoResponse detalhar(Long id) {
        return DistribuicaoResponse.de(obter(id));
    }

    private Distribuicao obter(Long id) {
        return repository.findByIdAndInstituicaoId(id, instituicaoAtual.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Distribuição não encontrada: id " + id));
    }
}
