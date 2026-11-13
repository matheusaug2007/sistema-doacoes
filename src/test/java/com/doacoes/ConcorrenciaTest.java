package com.doacoes;

import static org.assertj.core.api.Assertions.assertThat;

import com.doacoes.estoque.EstoqueInsuficienteException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.test.context.ActiveProfiles;

/** RN03 sob concorrência: entregas simultâneas nunca gastam o mesmo saldo duas vezes. */
@SpringBootTest
@ActiveProfiles("h2")
class ConcorrenciaTest {

    @Autowired
    private Cenario cenario;

    @Test
    void distribuicoesSimultaneasNuncaDeixamOSaldoNegativo() throws Exception {
        String sufixo = " " + System.nanoTime();
        Long familiaId = cenario.beneficiario("Família" + sufixo);
        Long arrozId = cenario.alimento("Arroz" + sufixo);
        cenario.doacao(cenario.doador("Doador" + sufixo), Cenario.item(arrozId, "10", null));

        // Oito pedidos de 3 kg disputam 10 kg: no máximo três podem ser atendidos.
        int pedidos = 8;
        ExecutorService executor = Executors.newFixedThreadPool(pedidos);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();
        for (int i = 0; i < pedidos; i++) {
            resultados.add(executor.submit(() -> {
                largada.await();
                try {
                    cenario.distribuicao(familiaId, arrozId, "3");
                    return true;
                } catch (EstoqueInsuficienteException | ConcurrencyFailureException recusada) {
                    return false;
                }
            }));
        }
        largada.countDown();

        int atendidos = 0;
        for (Future<Boolean> resultado : resultados) {
            if (resultado.get(60, TimeUnit.SECONDS)) {
                atendidos++;
            }
        }
        executor.shutdown();

        BigDecimal saldoEsperado = new BigDecimal("10").subtract(new BigDecimal("3").multiply(new BigDecimal(atendidos)));
        assertThat(atendidos).isBetween(1, 3);
        assertThat(cenario.saldo(arrozId).quantidadeDisponivel()).isEqualByComparingTo(saldoEsperado);
    }
}
