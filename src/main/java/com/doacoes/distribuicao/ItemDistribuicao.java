package com.doacoes.distribuicao;

import com.doacoes.doacao.ItemDoacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** Quanto de um lote foi entregue em uma distribuição. Liga a saída ao lote de origem. */
@Entity
@Table(name = "item_distribuicao")
public class ItemDistribuicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "distribuicao_id", nullable = false, updatable = false)
    private Distribuicao distribuicao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_doacao_id", nullable = false, updatable = false)
    private ItemDoacao lote;

    @Column(nullable = false, updatable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    protected ItemDistribuicao() {
    }

    ItemDistribuicao(Distribuicao distribuicao, ItemDoacao lote, BigDecimal quantidade) {
        this.distribuicao = distribuicao;
        this.lote = lote;
        this.quantidade = quantidade;
    }

    public Long getId() {
        return id;
    }

    public ItemDoacao getLote() {
        return lote;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }
}
