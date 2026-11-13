package com.doacoes;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Sistema de Gestão de Doações de Alimentos",
        version = "v1",
        description = "Cadastros, doações, distribuições e controle de estoque por lote."))
public class SistemaDoacoesApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaDoacoesApplication.class, args);
    }
}
