package com.doacoes.cadastro;

import static org.assertj.core.api.Assertions.assertThat;

import com.doacoes.Cenario;
import com.doacoes.cadastro.beneficiario.BeneficiarioService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class PessoaServiceTest {

    @Autowired
    private Cenario cenario;
    @Autowired
    private PessoaService pessoaService;
    @Autowired
    private BeneficiarioService beneficiarioService;

    @Test
    void buscaTrazDoadoresEBeneficiariosJuntosCadaUmDescritoDoSeuJeito() {
        Long doadorId = cenario.doador("Zulmira Mercearia");
        Long beneficiarioId = cenario.beneficiario("Família Zulmira");
        cenario.doador("Outro Doador");

        List<PessoaResumo> achados = pessoaService.buscar("zulmira", null);

        assertThat(achados).extracting(PessoaResumo::nome)
                .containsExactly("Família Zulmira", "Zulmira Mercearia");
        PessoaResumo beneficiario = achados.get(0);
        assertThat(beneficiario.tipoCadastro()).isEqualTo(TipoCadastro.BENEFICIARIO);
        assertThat(beneficiario.id()).isEqualTo(beneficiarioId);
        assertThat(beneficiario.dados()).isEqualTo("Beneficiário Família Zulmira (FAMILIA, 4 pessoa(s))");
        PessoaResumo doador = achados.get(1);
        assertThat(doador.tipoCadastro()).isEqualTo(TipoCadastro.DOADOR);
        assertThat(doador.id()).isEqualTo(doadorId);
        assertThat(doador.dados()).isEqualTo("Doador Zulmira Mercearia (SUPERMERCADO)");
    }

    @Test
    void buscaPodeFiltrarPorAtivos() {
        cenario.doador("Yolanda Hortifruti");
        Long inativo = cenario.beneficiario("Yolanda Silva");
        beneficiarioService.desativar(inativo);

        assertThat(pessoaService.buscar("yolanda", true)).extracting(PessoaResumo::nome)
                .containsExactly("Yolanda Hortifruti");
        assertThat(pessoaService.buscar("yolanda", false)).extracting(PessoaResumo::nome)
                .containsExactly("Yolanda Silva");
        assertThat(pessoaService.buscar("yolanda", null)).hasSize(2);
    }
}
