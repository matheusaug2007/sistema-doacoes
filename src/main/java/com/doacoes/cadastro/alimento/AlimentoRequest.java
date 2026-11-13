package com.doacoes.cadastro.alimento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlimentoRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Size(max = 60) String categoria,
        @NotNull UnidadeMedida unidadeMedida) {
}
