package com.doacoes.shared;

/** Identifica um cadastro citado dentro de outra resposta (ex.: o doador de uma doação). */
public record Referencia(Long id, String nome) {
}
