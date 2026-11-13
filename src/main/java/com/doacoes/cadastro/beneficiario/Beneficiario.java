package com.doacoes.cadastro.beneficiario;

import com.doacoes.cadastro.Pessoa;
import com.doacoes.cadastro.TipoCadastro;
import com.doacoes.cadastro.instituicao.Instituicao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "beneficiario")
public class Beneficiario extends Pessoa {

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private TipoBeneficiario tipo;

    /** Opcional de propósito: só deve ser coletado quando a instituição realmente precisar (LGPD). */
    @Column(length = 20)
    private String documento;

    /** Quantas pessoas este cadastro representa (ex.: tamanho da família). */
    @Column(nullable = false)
    private int quantidadePessoas = 1;

    @Column(length = 500)
    private String observacoes;

    protected Beneficiario() {
    }

    public Beneficiario(Instituicao instituicao) {
        super(instituicao);
    }

    @Override
    public String getDados() {
        return "Beneficiário " + getNome() + " (" + tipo + ", " + quantidadePessoas + " pessoa(s))";
    }

    @Override
    public TipoCadastro getTipoCadastro() {
        return TipoCadastro.BENEFICIARIO;
    }

    public TipoBeneficiario getTipo() {
        return tipo;
    }

    public void setTipo(TipoBeneficiario tipo) {
        this.tipo = tipo;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public int getQuantidadePessoas() {
        return quantidadePessoas;
    }

    public void setQuantidadePessoas(int quantidadePessoas) {
        this.quantidadePessoas = quantidadePessoas;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
