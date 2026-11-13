package com.doacoes.cadastro;

import static com.doacoes.Cenario.item;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.doacoes.Cenario;
import com.doacoes.cadastro.alimento.AlimentoRequest;
import com.doacoes.cadastro.alimento.AlimentoResponse;
import com.doacoes.cadastro.alimento.AlimentoService;
import com.doacoes.cadastro.alimento.UnidadeMedida;
import com.doacoes.cadastro.beneficiario.BeneficiarioRequest;
import com.doacoes.cadastro.beneficiario.BeneficiarioResponse;
import com.doacoes.cadastro.beneficiario.BeneficiarioService;
import com.doacoes.cadastro.beneficiario.TipoBeneficiario;
import com.doacoes.cadastro.doador.DoadorRequest;
import com.doacoes.cadastro.doador.DoadorResponse;
import com.doacoes.cadastro.doador.DoadorService;
import com.doacoes.cadastro.doador.TipoDoador;
import com.doacoes.shared.erro.ConflitoException;
import com.doacoes.shared.erro.RecursoNaoEncontradoException;
import com.doacoes.shared.erro.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class CadastroServiceTest {

    private static final Pageable POR_NOME = PageRequest.of(0, 50, Sort.by("nome"));

    @Autowired
    private Cenario cenario;
    @Autowired
    private DoadorService doadorService;
    @Autowired
    private BeneficiarioService beneficiarioService;
    @Autowired
    private AlimentoService alimentoService;

    @Test
    void criaAtualizaEDetalhaDoador() {
        DoadorResponse criado = doadorService.criar(new DoadorRequest("  Padaria Sol  ", TipoDoador.PESSOA_JURIDICA,
                "12.345.678/0001-90", "(44) 99999-0000", "contato@padariasol.example", "Rua A, 10", "  "));

        assertThat(criado.nome()).isEqualTo("Padaria Sol");
        assertThat(criado.observacoes()).isNull();
        assertThat(criado.ativo()).isTrue();

        doadorService.atualizar(criado.id(), new DoadorRequest("Padaria Sol Nascente", TipoDoador.PESSOA_JURIDICA,
                "12.345.678/0001-90", null, null, null, null));

        DoadorResponse atual = doadorService.detalhar(criado.id());
        assertThat(atual.nome()).isEqualTo("Padaria Sol Nascente");
        assertThat(atual.telefone()).isNull();
    }

    @Test
    void doisDoadoresNaoPodemTerOMesmoDocumento() {
        doadorService.criar(doadorComDocumento("Padaria Sol", "111"));
        DoadorResponse outro = doadorService.criar(doadorComDocumento("Padaria Lua", "222"));

        assertThatThrownBy(() -> doadorService.criar(doadorComDocumento("Padaria Estrela", "111")))
                .isInstanceOf(ConflitoException.class);
        assertThatThrownBy(() -> doadorService.atualizar(outro.id(), doadorComDocumento("Padaria Lua", "111")))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void variosCadastrosPodemFicarSemDocumento() {
        Long primeiro = cenario.doador("Anônimo 1");
        Long segundo = cenario.doador("Anônimo 2");

        assertThat(primeiro).isNotEqualTo(segundo);
    }

    @Test
    void excluirDesativaSemApagarEReativarDesfaz() {
        Long id = cenario.doador("Sacolão Zeta");

        doadorService.desativar(id);

        assertThat(doadorService.detalhar(id).ativo()).isFalse();
        assertThat(doadorService.listar(true, "zeta", POR_NOME).conteudo()).isEmpty();
        assertThat(doadorService.listar(false, "zeta", POR_NOME).conteudo()).hasSize(1);
        assertThat(doadorService.listar(null, "zeta", POR_NOME).conteudo()).hasSize(1);

        doadorService.reativar(id);

        assertThat(doadorService.listar(true, "ZETA", POR_NOME).conteudo()).hasSize(1);
    }

    @Test
    void cadastroInexistenteEhNaoEncontrado() {
        assertThatThrownBy(() -> doadorService.detalhar(-1L)).isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> beneficiarioService.detalhar(-1L)).isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> alimentoService.detalhar(-1L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void beneficiarioSemQuantidadeDePessoasRepresentaUmaPessoa() {
        BeneficiarioResponse criado = beneficiarioService.criar(
                new BeneficiarioRequest("João", TipoBeneficiario.PESSOA, null, null, null, null, null, null));

        assertThat(criado.quantidadePessoas()).isEqualTo(1);
    }

    @Test
    void doisAlimentosNaoPodemTerOMesmoNomeNemMudandoMaiusculas() {
        cenario.alimento("Macarrão");

        assertThatThrownBy(() -> cenario.alimento("MACARRÃO")).isInstanceOf(ConflitoException.class);
    }

    @Test
    void unidadeDoAlimentoSoMudaEnquantoNaoHaDoacoes() {
        Long oleoId = cenario.alimento("Óleo");

        AlimentoResponse emLitros = alimentoService.atualizar(oleoId,
                new AlimentoRequest("Óleo", "Mercearia", UnidadeMedida.L));
        assertThat(emLitros.unidadeMedida()).isEqualTo(UnidadeMedida.L);

        cenario.doacao(cenario.doador("Mercado Central"), item(oleoId, "6", null));

        assertThatThrownBy(() -> alimentoService.atualizar(oleoId,
                new AlimentoRequest("Óleo", "Mercearia", UnidadeMedida.UNIDADE)))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(alimentoService.atualizar(oleoId, new AlimentoRequest("Óleo de soja", "Mercearia",
                UnidadeMedida.L)).nome()).isEqualTo("Óleo de soja");
    }

    private static DoadorRequest doadorComDocumento(String nome, String documento) {
        return new DoadorRequest(nome, TipoDoador.PESSOA_JURIDICA, documento, null, null, null, null);
    }
}
