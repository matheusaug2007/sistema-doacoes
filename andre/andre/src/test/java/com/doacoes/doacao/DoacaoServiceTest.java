package com.doacoes.doacao;

import static com.doacoes.Cenario.item;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.doacoes.Cenario;
import com.doacoes.cadastro.alimento.AlimentoService;
import com.doacoes.cadastro.doador.DoadorService;
import com.doacoes.estoque.MovimentacaoEstoque;
import com.doacoes.estoque.MovimentacaoRepository;
import com.doacoes.estoque.TipoMovimentacao;
import com.doacoes.shared.StatusLancamento;
import com.doacoes.shared.erro.ConflitoException;
import com.doacoes.shared.erro.RegraNegocioException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class DoacaoServiceTest {

    @Autowired
    private Cenario cenario;
    @Autowired
    private DoacaoService doacaoService;
    @Autowired
    private DoadorService doadorService;
    @Autowired
    private AlimentoService alimentoService;
    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    private LocalDate hoje;
    private Long doadorId;
    private Long arrozId;

    @BeforeEach
    void montar() {
        hoje = cenario.hoje();
        doadorId = cenario.doador("Mercado Central");
        arrozId = cenario.alimento("Arroz");
    }

    @Test
    void cadaItemViraUmLoteComSaldoCheioEUmaEntradaNoLivro() {
        Long feijaoId = cenario.alimento("Feijão");

        DoacaoResponse doacao = cenario.doacao(doadorId,
                item(arrozId, "10", hoje.plusDays(30)), item(feijaoId, "4.5", null));

        assertThat(doacao.status()).isEqualTo(StatusLancamento.CONFIRMADA);
        assertThat(doacao.doador().nome()).isEqualTo("Mercado Central");
        assertThat(doacao.itens()).hasSize(2);
        DoacaoResponse.Item lote = doacao.itens().get(0);
        assertThat(lote.quantidade()).isEqualByComparingTo("10");
        assertThat(lote.quantidadeDisponivel()).isEqualByComparingTo("10");

        List<MovimentacaoEstoque> livro = movimentacaoRepository.findByLoteIdOrderById(lote.loteId());
        assertThat(livro).hasSize(1);
        assertThat(livro.get(0).getTipo()).isEqualTo(TipoMovimentacao.ENTRADA);
        assertThat(livro.get(0).getQuantidade()).isEqualByComparingTo("10");
        assertThat(cenario.saldo(arrozId).quantidadeDisponivel()).isEqualByComparingTo("10");
        assertThat(cenario.saldo(feijaoId).quantidadeDisponivel()).isEqualByComparingTo("4.5");
    }

    @Test
    void semDataInformadaADoacaoEDeHoje() {
        DoacaoResponse doacao = cenario.doacao(doadorId, (LocalDate) null, item(arrozId, "1", null));

        assertThat(doacao.dataRecebimento()).isEqualTo(hoje);
    }

    @Test
    void rejeitaDoadorInativo() {
        doadorService.desativar(doadorId);

        assertThatThrownBy(() -> cenario.doacao(doadorId, item(arrozId, "1", null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("inativo");
    }

    @Test
    void rejeitaAlimentoInativo() {
        alimentoService.desativar(arrozId);

        assertThatThrownBy(() -> cenario.doacao(doadorId, item(arrozId, "1", null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("inativo");
    }

    @Test
    void rejeitaAlimentoQueJaChegouVencido() {
        assertThatThrownBy(() -> cenario.doacao(doadorId, item(arrozId, "1", hoje.minusDays(1))))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("vencido");
    }

    @Test
    void rejeitaDataDeRecebimentoFutura() {
        assertThatThrownBy(() -> cenario.doacao(doadorId, hoje.plusDays(1), item(arrozId, "1", null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("futura");
    }

    @Test
    void cancelarDoacaoIntactaTiraOsLotesDoEstoque() {
        DoacaoResponse doacao = cenario.doacao(doadorId, item(arrozId, "10", null));

        DoacaoResponse cancelada = doacaoService.cancelar(doacao.id(), "Lançada em duplicidade");

        assertThat(cancelada.status()).isEqualTo(StatusLancamento.CANCELADA);
        assertThat(cancelada.motivoCancelamento()).isEqualTo("Lançada em duplicidade");
        assertThat(cancelada.itens().get(0).quantidadeDisponivel()).isEqualByComparingTo("0");
        assertThat(cenario.saldo(arrozId).quantidadeDisponivel()).isEqualByComparingTo("0");
        assertThat(movimentacaoRepository.findByLoteIdOrderById(doacao.itens().get(0).loteId()))
                .extracting(MovimentacaoEstoque::getTipo)
                .containsExactly(TipoMovimentacao.ENTRADA, TipoMovimentacao.ESTORNO_ENTRADA);
    }

    @Test
    void naoCancelaDoacaoDeQueJaSaiuAlimento() {
        DoacaoResponse doacao = cenario.doacao(doadorId, item(arrozId, "10", null));
        cenario.distribuicao(cenario.beneficiario("Família Souza"), arrozId, "2");

        assertThatThrownBy(() -> doacaoService.cancelar(doacao.id(), "Engano"))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("já teve distribuição ou descarte");
    }

    @Test
    void naoCancelaDuasVezes() {
        DoacaoResponse doacao = cenario.doacao(doadorId, item(arrozId, "10", null));
        doacaoService.cancelar(doacao.id(), "Engano");

        assertThatThrownBy(() -> doacaoService.cancelar(doacao.id(), "Engano de novo"))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("já está cancelada");
    }
}
