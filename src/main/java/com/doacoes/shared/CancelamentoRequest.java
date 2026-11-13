package com.doacoes.shared;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelamentoRequest(@NotBlank @Size(max = 255) String motivo) {
}
