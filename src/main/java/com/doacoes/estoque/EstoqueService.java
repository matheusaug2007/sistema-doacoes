package com.doacoes.estoque;

import com.doacoes.cadastro.alimento.Alimento;
import com.doacoes.cadastro.alimento.AlimentoService;
import com.doacoes.cadastro.instituicao.InstituicaoAtual;
import com.doacoes.doacao.ItemDoacao;
import com.doacoes.shared.Pagina;
import com.doacoes.shared.Quantidades;
import com.doacoes.shared.erro.ConflitoException;
import com.doacoes.shared.erro.RecursoNaoEncontradoException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Único lugar que altera o saldo dos lotes. Toda alteração grava também uma linha no livro
 * de movimentações, na mesma transação (RN05).
 */
@Service
public class EstoqueService {

    private final LoteRepository loteRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final AlimentoService alimentoService;
    private final InstituicaoAtual instituicaoAtual;
    private final Clock clock;

    public EstoqueService(LoteRepository loteRepository, MovimentacaoRepository movimentacaoRepository,
            AlimentoService alimentoService, InstituicaoAtual instituicaoAtual, Clock clock) {
        this.loteRepository = loteRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.alimentoService = alimentoService;
        this.instituicaoAtual = instituicaoAtual;
        this.clock = clock;
    }

    // ---- Operações chamadas por doação e distribuição, sempre dentro da transação delas ----

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarEntrada(ItemDoacao lote) {
        registrar(lote, TipoMovimentacao.ENTRADA, lote.getQuantidade(), null, null);
    }

    /** Retira do estoque os lotes de uma doação cancelada. Falha se algum já teve saída. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void estornarEntradas(Long doacaoId, String motivo) {
        for (ItemDoacao lote : loteRepository.travarPorDoacao(doacaoId)) {
            if (!lote.estaIntacto()) {
                throw new ConflitoException("A doação não pode ser cancelada: o lote " + lote.getId() + " de "
                        + lote.getAlimento().getNome() + " já teve distribuição ou descarte.");
            }
            registrar(lote, TipoMovimentacao.ESTORNO_ENTRADA, lote.getQuantidade(), motivo, null);
            lote.baixar(lote.getQuantidade());
        }
    }

    /**
     * Retira a quantidade pedida dos lotes do alimento, começando pelo que vence primeiro
     * (FEFO), e devolve quanto saiu de cada um. Lotes vencidos na data de referência ficam de
     * fora (RN06). Se o total disponível não cobre o pedido, nada é baixado (RN03).
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public List<Baixa> baixarPorFefo(Alimento alimento, BigDecimal quantidade, LocalDate dataReferencia,
            Long distribuicaoId) {
        List<ItemDoacao> lotes = loteRepository.travarDisponiveis(alimento.getId(), dataReferencia);
        BigDecimal disponivel = lotes.stream()
                .map(ItemDoacao::getQuantidadeDisponivel)
                .reduce(Quantidades.ZERO, BigDecimal::add);
        if (disponivel.compareTo(quantidade) < 0) {
            throw new EstoqueInsuficienteException(alimento.getId(), alimento.getNome(), quantidade, disponivel);
        }

        List<Baixa> baixas = new ArrayList<>();
        BigDecimal restante = Quantidades.normalizar(quantidade);
        for (ItemDoacao lote : lotes) {
            if (restante.signum() == 0) {
                break;
            }
            BigDecimal retirada = restante.min(lote.getQuantidadeDisponivel());
            lote.baixar(retirada);
            registrar(lote, TipoMovimentacao.SAIDA, retirada, null, distribuicaoId);
            baixas.add(new Baixa(lote, retirada));
            restante = restante.subtract(retirada);
        }
        return baixas;
    }

    /** Devolve ao lote o que saiu em uma distribuição cancelada. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void estornarSaida(Long loteId, BigDecimal quantidade, Long distribuicaoId, String motivo) {
        ItemDoacao lote = loteRepository.travarPorId(loteId).orElseThrow();
        lote.devolver(quantidade);
        registrar(lote, TipoMovimentacao.ESTORNO_SAIDA, quantidade, motivo, distribuicaoId);
    }

    // ---- Operações expostas pela API de estoque ----

    /** Dá baixa por vencimento, avaria ou perda. Sem quantidade informada, descarta todo o saldo do lote. */
    @Transactional
    public LoteResponse descartar(Long loteId, DescarteRequest dados) {
        ItemDoacao lote = loteRepository.travarPorId(loteId)
                .filter(l -> l.getAlimento().getInstituicao().getId().equals(instituicaoAtual.id()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote não encontrado: id " + loteId));
        BigDecimal quantidade = dados.quantidade() != null
                ? Quantidades.normalizar(dados.quantidade())
                : lote.getQuantidadeDisponivel();
        if (quantidade.signum() <= 0) {
            throw new ConflitoException("O lote " + loteId + " não tem saldo para descartar.");
        }
        lote.baixar(quantidade);
        registrar(lote, TipoMovimentacao.DESCARTE, quantidade, dados.motivo().strip(), null);
        return LoteResponse.de(lote, LocalDate.now(clock));
    }

    /** RF06: saldo de cada alimento, separando o que ainda pode ser distribuído do que já venceu. */
    @Transactional(readOnly = true)
    public List<SaldoAlimento> saldos() {
        LocalDate hoje = LocalDate.now(clock);
        Map<Long, List<ItemDoacao>> lotesPorAlimento = loteRepository.buscarComSaldo(instituicaoAtual.id())
                .stream()
                .collect(Collectors.groupingBy(lote -> lote.getAlimento().getId()));
        return alimentoService.todos().stream()
                .filter(alimento -> alimento.isAtivo() || lotesPorAlimento.containsKey(alimento.getId()))
                .map(alimento -> SaldoAlimento.de(alimento,
                        lotesPorAlimento.getOrDefault(alimento.getId(), List.of()), hoje))
                .toList();
    }

    /**
     * Lotes com saldo, do que vence antes para o que vence depois.
     *
     * @param vencidos true traz só os vencidos; false, só os válidos; null, todos
     * @param venceEmDias traz só os lotes válidos que vencem dentro desse número de dias
     */
    @Transactional(readOnly = true)
    public List<LoteResponse> lotes(Long alimentoId, Boolean vencidos, Integer venceEmDias) {
        LocalDate hoje = LocalDate.now(clock);
        return loteRepository.buscarComSaldo(instituicaoAtual.id()).stream()
                .filter(lote -> alimentoId == null || lote.getAlimento().getId().equals(alimentoId))
                .filter(lote -> vencidos == null || lote.estaVencidoEm(hoje) == vencidos)
                .filter(lote -> venceEmDias == null || venceAte(lote, hoje, hoje.plusDays(venceEmDias)))
                .map(lote -> LoteResponse.de(lote, hoje))
                .toList();
    }

    @Transactional(readOnly = true)
    public Pagina<MovimentacaoResponse> movimentacoes(Long alimentoId, TipoMovimentacao tipo, LocalDate de,
            LocalDate ate, Pageable pageable) {
        LocalDateTime inicio = de != null ? de.atStartOfDay() : null;
        LocalDateTime fim = ate != null ? ate.plusDays(1).atStartOfDay() : null;
        return Pagina.de(
                movimentacaoRepository.buscar(instituicaoAtual.id(), alimentoId, tipo, inicio, fim, pageable),
                MovimentacaoResponse::de);
    }

    private static boolean venceAte(ItemDoacao lote, LocalDate hoje, LocalDate limite) {
        LocalDate validade = lote.getDataValidade();
        return validade != null && !validade.isBefore(hoje) && !validade.isAfter(limite);
    }

    private void registrar(ItemDoacao lote, TipoMovimentacao tipo, BigDecimal quantidade, String motivo,
            Long distribuicaoId) {
        movimentacaoRepository.save(
                new MovimentacaoEstoque(lote, tipo, quantidade, motivo, distribuicaoId, LocalDateTime.now(clock)));
    }
}
