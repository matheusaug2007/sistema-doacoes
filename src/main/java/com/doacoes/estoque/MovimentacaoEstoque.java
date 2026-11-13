package com.doacoes.estoque;

import com.doacoes.doacao.ItemDoacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Linha do livro de estoque. Nunca é alterada nem apagada: um erro se corrige com uma nova
 * linha de estorno. O saldo de um lote é sempre a soma das suas movimentações.
 */
@Entity
@Table(name = "movimentacao_estoque")
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_doacao_id", nullable = false, updatable = false)
    private ItemDoacao lote;

    /** Preenchido nas saídas e nos seus estornos. */
    @Column(updatable = false)
    private Long distribuicaoId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, updatable = false, length = 20)
    private TipoMovimentacao tipo;

    @Column(nullable = false, updatable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    @Column(updatable = false, length = 255)
    private String motivo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected MovimentacaoEstoque() {
    }

    MovimentacaoEstoque(ItemDoacao lote, TipoMovimentacao tipo, BigDecimal quantidade, String motivo,
            Long distribuicaoId, LocalDateTime criadoEm) {
        this.lote = lote;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.motivo = motivo;
        this.distribuicaoId = distribuicaoId;
        this.criadoEm = criadoEm;
    }

    public Long getId() {
        return id;
    }

    public ItemDoacao getLote() {
        return lote;
    }

    public Long getDistribuicaoId() {
        return distribuicaoId;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
