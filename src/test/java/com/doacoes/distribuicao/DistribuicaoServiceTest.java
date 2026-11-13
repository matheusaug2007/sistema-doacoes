package com.doacoes.distribuicao;

import static com.doacoes.Cenario.item;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.doacoes.Cenario;
import com.doacoes.cadastro.beneficiario.BeneficiarioService;
import com.doacoes.doacao.DoacaoResponse;
import com.doacoes.estoque.DescarteRequest;
import com.doacoes.estoque.EstoqueInsuficienteException;
import com.doacoes.estoque.EstoqueService;
import com.doacoes.estoque.LoteRepository;
import com.doacoes.estoque.MovimentacaoRepository;
import com.doacoes.shared.StatusLancamento;
import com.doacoes.shared.erro.ConflitoException;
import com.doacoes.shared.erro.RegraNegocioException;
import java.math.BigDecimal;
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
class DistribuicaoServiceTest {

    @Autowired
    private Cenario cenario;
    @Autowired
    private DistribuicaoService distribuicaoService;
    @Autowired
    private BeneficiarioService beneficiarioService;
    @Autowired
    private EstoqueService estoqueService;
    @Autowired
    private LoteRepository loteRepository;
    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    private LocalDate hoje;
    private Long doadorId;
    private Long familiaId;
    private Long arrozId;

    @BeforeEach
    void montar() {
        hoje = cenario.hoje();
        doadorId = cenario.doador("Mercado Central");
        familiaId = cenario.beneficiario("Família Souza");
        arrozId = cenario.alimento("Arroz");
    }

    @Test
    void saiPrimeiroDoLoteQueVenceAntesEContinuaNoSeguinte() {
        Long venceDepois = loteDe("10", hoje.plusDays(30));
        Long venceAntes = loteDe("10", hoje.plusDays(5));

        DistribuicaoResponse distribuicao = cenario.distribuicao(familiaId, arrozId, "12");

        assertThat(distribuicao.itens()).extracting(DistribuicaoResponse.Item::loteId)
                .containsExactly(venceAntes, venceDepois);
        assertThat(distribuicao.itens().get(0).quantidade()).isEqualByComparingTo("10");
        assertThat(distribuicao.itens().get(1).quantidade()).isEqualByComparingTo("2");
        assertThat(saldoDoLote(venceAntes)).isEqualByComparingTo("0");
        assertThat(saldoDoLote(venceDepois)).isEqualByComparingTo("8");
        assertThat(cenario.saldo(arrozId).quantidadeDisponivel()).isEqualByComparingTo("8");
    }

    @Test
    void loteSemValidadeSaiPorUltimo() {
        Long semValidade = loteDe("10", null);
        Long comValidade = loteDe("10", hoje.plusDays(90));

        DistribuicaoResponse distribuicao = cenario.distribuicao(familiaId, arrozId, "3");

        assertThat(distribuicao.itens()).extracting(DistribuicaoResponse.Item::loteId).containsExactly(comValidade);
        assertThat(saldoDoLote(semValidade)).isEqualByComparingTo("10");
    }

    @Test
    void loteVencidoNaoEDistribuido() {
        Long vencido = cenario.doacao(doadorId, hoje.minusDays(10), item(arrozId, "10", hoje.minusDays(1)))
                .itens().get(0).loteId();
        loteDe("3", hoje.plusDays(5));

        assertThatThrownBy(() -> cenario.distribuicao(familiaId, arrozId, "5"))
                .isInstanceOfSatisfying(EstoqueInsuficienteException.class,
                        erro -> assertThat(erro.getDisponivel()).isEqualByComparingTo("3"));

        assertThat(saldoDoLote(vencido)).isEqualByComparingTo("10");
        assertThat(cenario.saldo(arrozId).quantidadeDisponivel()).isEqualByComparingTo("3");
        assertThat(cenario.saldo(arrozId).quantidadeVencida()).isEqualByComparingTo("10");
    }

    @Test
    void loteQueVenceHojeAindaPodeSair() {
        Long venceHoje = loteDe("2", hoje);

        DistribuicaoResponse distribuicao = cenario.distribuicao(familiaId, arrozId, "2");

        assertThat(distribuicao.itens()).extracting(DistribuicaoResponse.Item::loteId).containsExactly(venceHoje);
    }

    @Test
    void naoDistribuiMaisDoQueHaEmEstoque() {
        Long lote = loteDe("4", hoje.plusDays(5));

        assertThatThrownBy(() -> cenario.distribuicao(familiaId, arrozId, "4.001"))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .hasMessageContaining("Estoque insuficiente de Arroz");

        assertThat(saldoDoLote(lote)).isEqualByComparingTo("4");
    }

    @Test
    void pedidosRepetidosDoMesmoAlimentoSaoSomados() {
        Long lote = loteDe("10", hoje.plusDays(5));

        DistribuicaoResponse distribuicao = distribuicaoService.registrar(new DistribuicaoRequest(familiaId, null,
                null, List.of(new DistribuicaoRequest.Item(arrozId, new BigDecimal("2")),
                        new DistribuicaoRequest.Item(arrozId, new BigDecimal("3")))));

        assertThat(distribuicao.itens()).hasSize(1);
        assertThat(distribuicao.itens().get(0).quantidade()).isEqualByComparingTo("5");
        assertThat(saldoDoLote(lote)).isEqualByComparingTo("5");
    }

    @Test
    void rejeitaBeneficiarioInativo() {
        loteDe("10", null);
        beneficiarioService.desativar(familiaId);

        assertThatThrownBy(() -> cenario.distribuicao(familiaId, arrozId, "1"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("inativo");
    }

    @Test
    void cancelarDevolveAQuantidadeAoLoteDeOrigem() {
        Long lote = loteDe("10", hoje.plusDays(5));
        DistribuicaoResponse distribuicao = cenario.distribuicao(familiaId, arrozId, "7");

        DistribuicaoResponse cancelada = distribuicaoService.cancelar(distribuicao.id(), "Família não compareceu");

        assertThat(cancelada.status()).isEqualTo(StatusLancamento.CANCELADA);
        assertThat(saldoDoLote(lote)).isEqualByComparingTo("10");
        assertThatThrownBy(() -> distribuicaoService.cancelar(distribuicao.id(), "De novo"))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void oSaldoDoLoteESempreASomaDasSuasMovimentacoes() {
        Long lote = loteDe("10", hoje.plusDays(5));
        DistribuicaoResponse primeira = cenario.distribuicao(familiaId, arrozId, "4");
        cenario.distribuicao(familiaId, arrozId, "1.5");
        distribuicaoService.cancelar(primeira.id(), "Engano");
        estoqueService.descartar(lote, new DescarteRequest(new BigDecimal("2.25"), "Embalagem rasgada"));

        BigDecimal somaDoLivro = movimentacaoRepository.findByLoteIdOrderById(lote).stream()
                .map(m -> m.getTipo().aplicarSinal(m.getQuantidade()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(saldoDoLote(lote)).isEqualByComparingTo("6.25");
        assertThat(somaDoLivro).isEqualByComparingTo(saldoDoLote(lote));
    }

    private Long loteDe(String quantidade, LocalDate validade) {
        DoacaoResponse doacao = cenario.doacao(doadorId, item(arrozId, quantidade, validade));
        return doacao.itens().get(0).loteId();
    }

    private BigDecimal saldoDoLote(Long loteId) {
        return loteRepository.findById(loteId).orElseThrow().getQuantidadeDisponivel();
    }
}
