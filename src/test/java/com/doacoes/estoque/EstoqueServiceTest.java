package com.doacoes.estoque;

import static com.doacoes.Cenario.item;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.doacoes.Cenario;
import com.doacoes.cadastro.alimento.AlimentoService;
import com.doacoes.shared.Pagina;
import com.doacoes.shared.erro.ConflitoException;
import com.doacoes.shared.erro.RecursoNaoEncontradoException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class EstoqueServiceTest {

    @Autowired
    private Cenario cenario;
    @Autowired
    private EstoqueService estoqueService;
    @Autowired
    private AlimentoService alimentoService;

    private LocalDate hoje;
    private Long doadorId;
    private Long leiteId;

    @BeforeEach
    void montar() {
        hoje = cenario.hoje();
        doadorId = cenario.doador("Laticínios Serra");
        leiteId = cenario.alimento("Leite");
    }

    @Test
    void descartarParteDoLoteBaixaOSaldoERegistraOMotivo() {
        Long lote = loteDe("10", hoje.plusDays(3));

        LoteResponse depois = estoqueService.descartar(lote,
                new DescarteRequest(new BigDecimal("4"), "  Caixas amassadas  "));

        assertThat(depois.quantidadeDisponivel()).isEqualByComparingTo("6");
        MovimentacaoResponse descarte = movimentacoesDoLeite(TipoMovimentacao.DESCARTE).conteudo().get(0);
        assertThat(descarte.motivo()).isEqualTo("Caixas amassadas");
        assertThat(descarte.variacao()).isEqualByComparingTo("-4");
    }

    @Test
    void descartarSemQuantidadeZeraOLote() {
        Long vencido = cenario.doacao(doadorId, hoje.minusDays(10), item(leiteId, "8", hoje.minusDays(2)))
                .itens().get(0).loteId();

        LoteResponse depois = estoqueService.descartar(vencido, new DescarteRequest(null, "Vencido"));

        assertThat(depois.quantidadeDisponivel()).isEqualByComparingTo("0");
        assertThat(cenario.saldo(leiteId).quantidadeVencida()).isEqualByComparingTo("0");
        assertThatThrownBy(() -> estoqueService.descartar(vencido, new DescarteRequest(null, "Vencido")))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("não tem saldo");
    }

    @Test
    void naoDescartaMaisDoQueOSaldoDoLote() {
        Long lote = loteDe("3", null);

        assertThatThrownBy(() -> estoqueService.descartar(lote, new DescarteRequest(new BigDecimal("3.5"), "Perda")))
                .isInstanceOf(EstoqueInsuficienteException.class);
    }

    @Test
    void descartarLoteInexistenteEhNaoEncontrado() {
        assertThatThrownBy(() -> estoqueService.descartar(-1L, new DescarteRequest(null, "Perda")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void saldoSeparaODisponivelDoVencidoEApontaOProximoVencimento() {
        loteDe("5", hoje.plusDays(20));
        loteDe("2", hoje.plusDays(4));
        loteDe("1", null);
        cenario.doacao(doadorId, hoje.minusDays(10), item(leiteId, "7", hoje.minusDays(1)));

        SaldoAlimento saldo = cenario.saldo(leiteId);

        assertThat(saldo.quantidadeDisponivel()).isEqualByComparingTo("8");
        assertThat(saldo.quantidadeVencida()).isEqualByComparingTo("7");
        assertThat(saldo.proximoVencimento()).isEqualTo(hoje.plusDays(4));
        assertThat(saldo.lotes()).isEqualTo(3);
    }

    @Test
    void alimentoSemEstoqueApareceZeradoEInativoSemEstoqueSome() {
        assertThat(cenario.saldo(leiteId).quantidadeDisponivel()).isEqualByComparingTo("0");

        alimentoService.desativar(leiteId);

        assertThat(estoqueService.saldos()).noneMatch(saldo -> saldo.alimentoId().equals(leiteId));
    }

    @Test
    void lotesPodemSerFiltradosPorVencidosEPorPrazoDeVencimento() {
        Long venceLogo = loteDe("2", hoje.plusDays(4));
        Long venceLonge = loteDe("5", hoje.plusDays(20));
        Long vencido = cenario.doacao(doadorId, hoje.minusDays(10), item(leiteId, "7", hoje.minusDays(1)))
                .itens().get(0).loteId();

        assertThat(estoqueService.lotes(leiteId, null, null)).extracting(LoteResponse::loteId)
                .containsExactly(vencido, venceLogo, venceLonge);
        assertThat(estoqueService.lotes(leiteId, true, null)).extracting(LoteResponse::loteId)
                .containsExactly(vencido);
        assertThat(estoqueService.lotes(leiteId, false, null)).extracting(LoteResponse::loteId)
                .containsExactly(venceLogo, venceLonge);
        assertThat(estoqueService.lotes(leiteId, null, 7)).extracting(LoteResponse::loteId)
                .containsExactly(venceLogo);
    }

    @Test
    void movimentacoesPodemSerFiltradasPorTipoEPorPeriodo() {
        loteDe("10", null);
        cenario.distribuicao(cenario.beneficiario("Família Lima"), leiteId, "3");

        assertThat(movimentacoesDoLeite(null).totalElementos()).isEqualTo(2);
        assertThat(movimentacoesDoLeite(TipoMovimentacao.SAIDA).conteudo())
                .singleElement()
                .satisfies(saida -> assertThat(saida.distribuicaoId()).isNotNull());
        assertThat(estoqueService.movimentacoes(leiteId, null, hoje.plusDays(1), null, pagina()).conteudo())
                .isEmpty();
        assertThat(estoqueService.movimentacoes(leiteId, null, hoje, hoje, pagina()).conteudo()).hasSize(2);
    }

    private Long loteDe(String quantidade, LocalDate validade) {
        return cenario.doacao(doadorId, item(leiteId, quantidade, validade)).itens().get(0).loteId();
    }

    private Pagina<MovimentacaoResponse> movimentacoesDoLeite(TipoMovimentacao tipo) {
        return estoqueService.movimentacoes(leiteId, tipo, null, null, pagina());
    }

    private static PageRequest pagina() {
        return PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "criadoEm", "id"));
    }
}
