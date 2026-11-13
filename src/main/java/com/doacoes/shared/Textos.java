package com.doacoes.shared;

public final class Textos {

    private Textos() {
    }

    /** Remove espaços das pontas e transforma texto vazio em null. */
    public static String limpar(String texto) {
        if (texto == null) {
            return null;
        }
        String limpo = texto.strip();
        return limpo.isEmpty() ? null : limpo;
    }

    /** Padrão para LIKE em minúsculas; sem termo de busca, casa com tudo. */
    public static String padraoDeBusca(String busca) {
        String termo = limpar(busca);
        return termo == null ? "%" : "%" + termo.toLowerCase() + "%";
    }
}
