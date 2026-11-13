package com.doacoes.shared;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RelogioConfig {

    /**
     * Relógio único da aplicação: "hoje" segue o fuso configurado, não o do servidor. Marca o
     * tempo em microssegundos, a mesma precisão das colunas DATETIME(6) do banco.
     */
    @Bean
    Clock clock(@Value("${app.fuso-horario:America/Sao_Paulo}") String fusoHorario) {
        return Clock.tick(Clock.system(ZoneId.of(fusoHorario)), Duration.ofNanos(1000));
    }
}
