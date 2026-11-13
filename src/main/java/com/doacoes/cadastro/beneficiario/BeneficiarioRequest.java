package com.doacoes.cadastro.beneficiario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BeneficiarioRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotNull TipoBeneficiario tipo,
        @Size(max = 20) String documento,
        @Size(max = 20) String telefone,
        @Email @Size(max = 150) String email,
        @Size(max = 255) String endereco,
        @Min(1) Integer quantidadePessoas,
        @Size(max = 500) String observacoes) {
}
