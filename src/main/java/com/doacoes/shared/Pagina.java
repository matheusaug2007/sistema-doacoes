package com.doacoes.shared;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/** Formato estável de resposta paginada, independente das classes internas do Spring Data. */
public record Pagina<T>(List<T> conteudo, int pagina, int tamanho, long totalElementos, int totalPaginas) {

    public static <E, T> Pagina<T> de(Page<E> page, Function<? super E, ? extends T> conversor) {
        List<T> conteudo = page.getContent().stream().<T>map(conversor).toList();
        return new Pagina<>(conteudo, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
