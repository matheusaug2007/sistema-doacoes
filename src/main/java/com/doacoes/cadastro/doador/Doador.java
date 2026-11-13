package com.doacoes.cadastro.doador;

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
@Table(name = "doador")
public class Doador extends Pessoa {

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private TipoDoador tipo;

    /** CPF ou CNPJ, conforme o tipo. Opcional. */
    @Column(length = 20)
    private String documento;

    @Column(length = 500)
    private String observacoes;

    protected Doador() {
    }

    public Doador(Instituicao instituicao) {
        super(instituicao);
    }

    @Override
    public String getDados() {
        return "Doador " + getNome() + " (" + tipo + ")";
    }

    @Override
    public TipoCadastro getTipoCadastro() {
        return TipoCadastro.DOADOR;
    }

    public TipoDoador getTipo() {
        return tipo;
    }

    public void setTipo(TipoDoador tipo) {
        this.tipo = tipo;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
