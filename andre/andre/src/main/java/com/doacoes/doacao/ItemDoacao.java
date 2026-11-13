package com.doacoes.doacao;

import com.doacoes.cadastro.alimento.Alimento;
import com.doacoes.estoque.EstoqueInsuficienteException;
import com.doacoes.shared.Quantidades;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Item de uma doação e, ao mesmo tempo, um lote do estoque: o mesmo alimento pode chegar
 * várias vezes, cada vez com a sua validade, e as saídas são descontadas lote a lote.
 */
@Entity
@Table(name = "item_doacao")
public class ItemDoacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doacao_id", nullable = false, updatable = false)
    private Doacao doacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alimento_id", nullable = false, updatable = false)
    private Alimento alimento;

    /** Quantidade recebida. Não muda depois do registro. */
    @Column(nullable = false, updatable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    /** Saldo atual do lote: o recebido menos tudo o que já saiu. */
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidadeDisponivel;

    @Column(updatable = false)
    private LocalDate dataValidade;

    // Segunda barreira contra gravações concorrentes, além da trava de linha usada nas baixas.
    @Version
    private long versao;

    protected ItemDoacao() {
    }

    ItemDoacao(Doacao doacao, Alimento alimento, BigDecimal quantidade, LocalDate dataValidade) {
        this.doacao = doacao;
        this.alimento = alimento;
        this.quantidade = Quantidades.normalizar(quantidade);
        this.quantidadeDisponivel = this.quantidade;
        this.dataValidade = dataValidade;
    }

    /** RN06: um lote vencido não pode ser distribuído. Lote sem validade nunca vence. */
    public boolean estaVencidoEm(LocalDate data) {
        return dataValidade != null && dataValidade.isBefore(data);
    }

    /** Nada saiu deste lote ainda? */
    public boolean estaIntacto() {
        return quantidadeDisponivel.compareTo(quantidade) == 0;
    }

    /** RN03: nunca sai mais do que o lote tem. */
    public void baixar(BigDecimal quantidadeRetirada) {
        if (quantidadeRetirada.compareTo(quantidadeDisponivel) > 0) {
            throw new EstoqueInsuficienteException(alimento.getId(), alimento.getNome(), quantidadeRetirada,
                    quantidadeDisponivel);
        }
        quantidadeDisponivel = quantidadeDisponivel.subtract(quantidadeRetirada);
    }

    /** Devolve ao lote o que havia saído (cancelamento de uma distribuição). */
    public void devolver(BigDecimal quantidadeDevolvida) {
        BigDecimal novoSaldo = quantidadeDisponivel.add(quantidadeDevolvida);
        if (novoSaldo.compareTo(quantidade) > 0) {
            throw new IllegalStateException("Devolução maior do que o que saiu do lote " + id);
        }
        quantidadeDisponivel = novoSaldo;
    }

    public Long getId() {
        return id;
    }

    public Doacao getDoacao() {
        return doacao;
    }

    public Alimento getAlimento() {
        return alimento;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public BigDecimal getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }
}
