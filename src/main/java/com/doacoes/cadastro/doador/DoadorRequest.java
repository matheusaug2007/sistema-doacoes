package com.doacoes.cadastro.doador;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DoadorRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotNull TipoDoador tipo,
        @Size(max = 20) String documento,
        @Size(max = 20) String telefone,
        @Email @Size(max = 150) String email,
        @Size(max = 255) String endereco,
        @Size(max = 500) String observacoes) {
}
