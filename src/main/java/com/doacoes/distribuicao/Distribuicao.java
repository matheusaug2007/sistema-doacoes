package com.doacoes.distribuicao;

import com.doacoes.cadastro.beneficiario.Beneficiario;
import com.doacoes.cadastro.instituicao.Instituicao;
import com.doacoes.doacao.ItemDoacao;
import com.doacoes.shared.StatusLancamento;
import com.doacoes.shared.erro.ConflitoException;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "distribuicao")
public class Distribuicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instituicao_id", nullable = false, updatable = false)
    private Instituicao instituicao;

    // RN04: toda distribuição tem um beneficiário.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beneficiario_id", nullable = false, updatable = false)
    private Beneficiario beneficiario;

    @Column(nullable = false)
    private LocalDate dataDistribuicao;

    @Column(length = 500)
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private StatusLancamento status = StatusLancamento.CONFIRMADA;

    @Column(length = 255)
    private String motivoCancelamento;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    private LocalDateTime canceladoEm;

    // Composição: os itens só existem dentro da distribuição e são gravados junto com ela.
    @OneToMany(mappedBy = "distribuicao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ItemDistribuicao> itens = new ArrayList<>();

    protected Distribuicao() {
    }

    public Distribuicao(Instituicao instituicao, Beneficiario beneficiario, LocalDate dataDistribuicao,
            String observacoes, LocalDateTime criadoEm) {
        this.instituicao = instituicao;
        this.beneficiario = beneficiario;
        this.dataDistribuicao = dataDistribuicao;
        this.observacoes = observacoes;
        this.criadoEm = criadoEm;
    }

    /** Registra quanto foi entregue de um lote. */
    public ItemDistribuicao adicionarItem(ItemDoacao lote, BigDecimal quantidade) {
        ItemDistribuicao item = new ItemDistribuicao(this, lote, quantidade);
        itens.add(item);
        return item;
    }

    public void cancelar(String motivo, LocalDateTime agora) {
        if (status == StatusLancamento.CANCELADA) {
            throw new ConflitoException("A distribuição " + id + " já está cancelada.");
        }
        status = StatusLancamento.CANCELADA;
        motivoCancelamento = motivo;
        canceladoEm = agora;
    }

    public Long getId() {
        return id;
    }

    public Beneficiario getBeneficiario() {
        return beneficiario;
    }

    public LocalDate getDataDistribuicao() {
        return dataDistribuicao;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public StatusLancamento getStatus() {
        return status;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getCanceladoEm() {
        return canceladoEm;
    }

    public List<ItemDistribuicao> getItens() {
        return Collections.unmodifiableList(itens);
    }
}
