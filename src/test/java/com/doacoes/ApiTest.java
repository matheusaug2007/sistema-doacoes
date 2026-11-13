package com.doacoes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Exercita a API de ponta a ponta, do JSON ao banco. Estes testes gravam de verdade (sem
 * rollback), por isso cada um usa nomes únicos.
 */
@SpringBootTest
@ActiveProfiles("h2")
class ApiTest {

    @Autowired
    private WebApplicationContext contexto;
    @Autowired
    private Cenario cenario;

    private MockMvc mvc;
    private String sufixo;

    @BeforeEach
    void montar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
        sufixo = " " + System.nanoTime();
    }

    @Test
    void fluxoCompletoDaDoacaoAteADistribuicao() throws Exception {
        long doadorId = criar("/api/v1/doadores", """
                {"nome": "Mercado%s", "tipo": "SUPERMERCADO", "email": "contato@mercado.example"}
                """.formatted(sufixo));
        long arrozId = criar("/api/v1/alimentos", """
                {"nome": "Arroz%s", "categoria": "Grãos", "unidadeMedida": "KG"}
                """.formatted(sufixo));
        long familiaId = criar("/api/v1/beneficiarios", """
                {"nome": "Família%s", "tipo": "FAMILIA", "quantidadePessoas": 5}
                """.formatted(sufixo));

        mvc.perform(postJson("/api/v1/doacoes", """
                        {"doadorId": %d, "itens": [{"alimentoId": %d, "quantidade": 20, "dataValidade": "%s"}]}
                        """.formatted(doadorId, arrozId, cenario.hoje().plusDays(60))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"))
                .andExpect(jsonPath("$.doador.id").value(doadorId))
                .andExpect(jsonPath("$.itens[0].quantidadeDisponivel").value(20.0));

        mvc.perform(get("/api/v1/estoque"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.alimentoId == %d)].quantidadeDisponivel".formatted(arrozId)).value(20.0));

        String distribuicao = mvc.perform(postJson("/api/v1/distribuicoes", """
                        {"beneficiarioId": %d, "itens": [{"alimentoId": %d, "quantidade": 7.5}]}
                        """.formatted(familiaId, arrozId)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.beneficiario.id").value(familiaId))
                .andExpect(jsonPath("$.itens[0].quantidade").value(7.5))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        long distribuicaoId = id(distribuicao);

        mvc.perform(postJson("/api/v1/distribuicoes", """
                        {"beneficiarioId": %d, "itens": [{"alimentoId": %d, "quantidade": 13}]}
                        """.formatted(familiaId, arrozId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito"))
                .andExpect(jsonPath("$.alimentoId").value(arrozId))
                .andExpect(jsonPath("$.disponivel").value(12.5));

        mvc.perform(get("/api/v1/doacoes").param("doadorId", String.valueOf(doadorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].itens[0].quantidadeDisponivel").value(12.5));

        mvc.perform(get("/api/v1/distribuicoes")
                        .param("beneficiarioId", String.valueOf(familiaId))
                        .param("de", cenario.hoje().toString())
                        .param("ate", cenario.hoje().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1));

        mvc.perform(postJson("/api/v1/distribuicoes/" + distribuicaoId + "/cancelamento", """
                        {"motivo": "Família não compareceu"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        mvc.perform(get("/api/v1/estoque/lotes").param("alimentoId", String.valueOf(arrozId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantidadeDisponivel").value(20.0))
                .andExpect(jsonPath("$[0].vencido").value(false));

        mvc.perform(get("/api/v1/estoque/movimentacoes")
                        .param("alimentoId", String.valueOf(arrozId))
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[*].tipo").value(
                        org.hamcrest.Matchers.contains("ENTRADA", "SAIDA", "ESTORNO_SAIDA")));
    }

    @Test
    void distribuicaoComUmAlimentoEmFaltaNaoBaixaNenhum() throws Exception {
        Long doadorId = cenario.doador("Doador" + sufixo);
        Long familiaId = cenario.beneficiario("Família" + sufixo);
        Long arrozId = cenario.alimento("Arroz" + sufixo);
        Long feijaoId = cenario.alimento("Feijão" + sufixo);
        cenario.doacao(doadorId, Cenario.item(arrozId, "10", null), Cenario.item(feijaoId, "1", null));

        mvc.perform(postJson("/api/v1/distribuicoes", """
                        {"beneficiarioId": %d, "itens": [
                          {"alimentoId": %d, "quantidade": 5},
                          {"alimentoId": %d, "quantidade": 2}]}
                        """.formatted(familiaId, arrozId, feijaoId)))
                .andExpect(status().isConflict());

        assertThat(cenario.saldo(arrozId).quantidadeDisponivel()).isEqualByComparingTo("10");
        assertThat(cenario.saldo(feijaoId).quantidadeDisponivel()).isEqualByComparingTo("1");
        mvc.perform(get("/api/v1/distribuicoes").param("beneficiarioId", String.valueOf(familiaId)))
                .andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    void doacaoPrecisaDeDoadorEDePeloMenosUmItem() throws Exception {
        mvc.perform(postJson("/api/v1/doacoes", """
                        {"itens": []}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.erros[*].campo").value(
                        org.hamcrest.Matchers.containsInAnyOrder("doadorId", "itens")));
    }

    @Test
    void quantidadeDeveSerPositivaComAteTresCasasDecimais() throws Exception {
        mvc.perform(postJson("/api/v1/doacoes", """
                        {"doadorId": 1, "itens": [
                          {"alimentoId": 1, "quantidade": 0},
                          {"alimentoId": 1, "quantidade": 1.2345}]}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo").value(
                        org.hamcrest.Matchers.containsInAnyOrder("itens[0].quantidade", "itens[1].quantidade")));
    }

    @Test
    void doadorInativoNaoPodeDoar() throws Exception {
        Long doadorId = cenario.doador("Doador" + sufixo);
        Long arrozId = cenario.alimento("Arroz" + sufixo);

        mvc.perform(delete("/api/v1/doadores/" + doadorId)).andExpect(status().isNoContent());

        mvc.perform(postJson("/api/v1/doacoes", """
                        {"doadorId": %d, "itens": [{"alimentoId": %d, "quantidade": 1}]}
                        """.formatted(doadorId, arrozId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Regra de negócio violada"));

        mvc.perform(post("/api/v1/doadores/" + doadorId + "/reativacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void cadastroInexistenteDevolve404() throws Exception {
        mvc.perform(get("/api/v1/alimentos/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"));
    }

    @Test
    void nomeDeAlimentoRepetidoDevolve409() throws Exception {
        String corpo = """
                {"nome": "Farinha%s", "categoria": "Grãos", "unidadeMedida": "KG"}
                """.formatted(sufixo);
        criar("/api/v1/alimentos", corpo);

        mvc.perform(postJson("/api/v1/alimentos", corpo)).andExpect(status().isConflict());
    }

    @Test
    void ordenacaoPorCampoInexistenteDevolve400() throws Exception {
        mvc.perform(get("/api/v1/doadores").param("sort", "naoExiste"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscaDePessoasMisturaDoadoresEBeneficiarios() throws Exception {
        cenario.doador("Mercado" + sufixo);
        cenario.beneficiario("Família" + sufixo);

        mvc.perform(get("/api/v1/pessoas").param("busca", sufixo.strip()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tipoCadastro").value(
                        org.hamcrest.Matchers.contains("BENEFICIARIO", "DOADOR")))
                .andExpect(jsonPath("$[1].dados").value("Doador Mercado" + sufixo + " (SUPERMERCADO)"));
    }

    @Test
    void dadosDaInstituicaoPodemSerPreenchidos() throws Exception {
        mvc.perform(put("/api/v1/instituicao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Associação Mãos Unidas", "responsavel": "Ana"}
                                """))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/instituicao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Associação Mãos Unidas"))
                .andExpect(jsonPath("$.responsavel").value("Ana"));
    }

    private long criar(String caminho, String corpo) throws Exception {
        String resposta = mvc.perform(postJson(caminho, corpo))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return id(resposta);
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder postJson(
            String caminho, String corpo) {
        return post(caminho).contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    private static long id(String json) {
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }
}
