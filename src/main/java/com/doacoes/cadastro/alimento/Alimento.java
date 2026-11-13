package com.doacoes.cadastro.alimento;

import com.doacoes.cadastro.instituicao.Instituicao;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Tipo de alimento do catálogo (ex.: "Arroz", em KG). Validade e quantidade não ficam aqui:
 * pertencem a cada lote recebido (ItemDoacao), porque o mesmo alimento chega várias vezes
 * com validades diferentes.
 */
@Entity
@Table(name = "alimento")
public class Alimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instituicao_id", nullable = false, updatable = false)
    private Instituicao instituicao;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 60)
    private String categoria;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private UnidadeMedida unidadeMedida;

    @Column(nullable = false)
    private boolean ativo = true;

    protected Alimento() {
    }

    public Alimento(Instituicao instituicao) {
        this.instituicao = instituicao;
    }

    public Long getId() {
        return id;
    }

    public Instituicao getInstituicao() {
        return instituicao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public UnidadeMedida getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(UnidadeMedida unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
