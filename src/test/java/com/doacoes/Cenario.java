package com.doacoes;

import com.doacoes.cadastro.alimento.AlimentoRequest;
import com.doacoes.cadastro.alimento.AlimentoService;
import com.doacoes.cadastro.alimento.UnidadeMedida;
import com.doacoes.cadastro.beneficiario.BeneficiarioRequest;
import com.doacoes.cadastro.beneficiario.BeneficiarioService;
import com.doacoes.cadastro.beneficiario.TipoBeneficiario;
import com.doacoes.cadastro.doador.DoadorRequest;
import com.doacoes.cadastro.doador.DoadorService;
import com.doacoes.cadastro.doador.TipoDoador;
import com.doacoes.distribuicao.DistribuicaoRequest;
import com.doacoes.distribuicao.DistribuicaoResponse;
import com.doacoes.distribuicao.DistribuicaoService;
import com.doacoes.doacao.DoacaoRequest;
import com.doacoes.doacao.DoacaoResponse;
import com.doacoes.doacao.DoacaoService;
import com.doacoes.estoque.EstoqueService;
import com.doacoes.estoque.SaldoAlimento;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

/** Atalhos para montar dados nos testes, sempre pelos mesmos serviços que a API usa. */
@Component
public class Cenario {

    private final DoadorService doadorService;
    private final BeneficiarioService beneficiarioService;
    private final AlimentoService alimentoService;
    private final DoacaoService doacaoService;
    private final DistribuicaoService distribuicaoService;
    private final EstoqueService estoqueService;
    private final Clock clock;

    public Cenario(DoadorService doadorService, BeneficiarioService beneficiarioService,
            AlimentoService alimentoService, DoacaoService doacaoService,
            DistribuicaoService distribuicaoService, EstoqueService estoqueService, Clock clock) {
        this.doadorService = doadorService;
        this.beneficiarioService = beneficiarioService;
        this.alimentoService = alimentoService;
        this.doacaoService = doacaoService;
        this.distribuicaoService = distribuicaoService;
        this.estoqueService = estoqueService;
        this.clock = clock;
    }

    public LocalDate hoje() {
        return LocalDate.now(clock);
    }

    public Long doador(String nome) {
        return doadorService.criar(new DoadorRequest(nome, TipoDoador.SUPERMERCADO, null, null, null, null, null))
                .id();
    }

    public Long beneficiario(String nome) {
        return beneficiarioService
                .criar(new BeneficiarioRequest(nome, TipoBeneficiario.FAMILIA, null, null, null, null, 4, null))
                .id();
    }

    public Long alimento(String nome) {
        return alimentoService.criar(new AlimentoRequest(nome, "Grãos", UnidadeMedida.KG)).id();
    }

    public static DoacaoRequest.Item item(Long alimentoId, String quantidade, LocalDate dataValidade) {
        return new DoacaoRequest.Item(alimentoId, new BigDecimal(quantidade), dataValidade);
    }

    /** Doação recebida hoje. */
    public DoacaoResponse doacao(Long doadorId, DoacaoRequest.Item... itens) {
        return doacao(doadorId, hoje(), itens);
    }

    public DoacaoResponse doacao(Long doadorId, LocalDate dataRecebimento, DoacaoRequest.Item... itens) {
        return doacaoService.registrar(new DoacaoRequest(doadorId, dataRecebimento, null, List.of(itens)));
    }

    public DistribuicaoResponse distribuicao(Long beneficiarioId, Long alimentoId, String quantidade) {
        return distribuicaoService.registrar(new DistribuicaoRequest(beneficiarioId, null, null,
                List.of(new DistribuicaoRequest.Item(alimentoId, new BigDecimal(quantidade)))));
    }

    public SaldoAlimento saldo(Long alimentoId) {
        return estoqueService.saldos().stream()
                .filter(saldo -> saldo.alimentoId().equals(alimentoId))
                .findFirst()
                .orElseThrow();
    }
}
