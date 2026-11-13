package com.doacoes.doacao;

import com.doacoes.cadastro.alimento.Alimento;
import com.doacoes.cadastro.doador.Doador;
import com.doacoes.cadastro.instituicao.Instituicao;
import com.doacoes.shared.StatusLancamento;
import com.doacoes.shared.erro.ConflitoException;
import com.doacoes.shared.erro.RegraNegocioException;
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
@Table(name = "doacao")
public class Doacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instituicao_id", nullable = false, updatable = false)
    private Instituicao instituicao;

    // RN01: toda doação tem um doador.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doador_id", nullable = false, updatable = false)
    private Doador doador;

    @Column(nullable = false)
    private LocalDate dataRecebimento;

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

    // Composição: os itens só existem dentro da doação e são gravados junto com ela.
    @OneToMany(mappedBy = "doacao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ItemDoacao> itens = new ArrayList<>();

    protected Doacao() {
    }

    public Doacao(Instituicao instituicao, Doador doador, LocalDate dataRecebimento, String observacoes,
            LocalDateTime criadoEm) {
        this.instituicao = instituicao;
        this.doador = doador;
        this.dataRecebimento = dataRecebimento;
        this.observacoes = observacoes;
        this.criadoEm = criadoEm;
    }

    /** Acrescenta um lote à doação. A validade é opcional (alimento sem data impressa). */
    public ItemDoacao adicionarItem(Alimento alimento, BigDecimal quantidade, LocalDate dataValidade) {
        if (dataValidade != null && dataValidade.isBefore(dataRecebimento)) {
            throw new RegraNegocioException(
                    "O alimento " + alimento.getNome() + " já estava vencido na data de recebimento.");
        }
        ItemDoacao item = new ItemDoacao(this, alimento, quantidade, dataValidade);
        itens.add(item);
        return item;
    }

    public void cancelar(String motivo, LocalDateTime agora) {
        if (status == StatusLancamento.CANCELADA) {
            throw new ConflitoException("A doação " + id + " já está cancelada.");
        }
        status = StatusLancamento.CANCELADA;
        motivoCancelamento = motivo;
        canceladoEm = agora;
    }

    public Long getId() {
        return id;
    }

    public Doador getDoador() {
        return doador;
    }

    public LocalDate getDataRecebimento() {
        return dataRecebimento;
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

    public List<ItemDoacao> getItens() {
        return Collections.unmodifiableList(itens);
    }
}
