package com.doacoes.cadastro;

import com.doacoes.cadastro.instituicao.Instituicao;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

/**
 * Dados comuns a doadores e beneficiários. Como @MappedSuperclass, a herança existe só no
 * código: cada subclasse continua com a sua própria tabela, como no DER.
 */
@MappedSuperclass
public abstract class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instituicao_id", nullable = false, updatable = false)
    private Instituicao instituicao;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(length = 255)
    private String endereco;

    // A exclusão é lógica (RF09): o cadastro some das listas, mas o histórico continua íntegro.
    @Column(nullable = false)
    private boolean ativo = true;

    protected Pessoa() {
    }

    protected Pessoa(Instituicao instituicao) {
        this.instituicao = instituicao;
    }

    /** Resumo legível do cadastro; cada subclasse acrescenta o que lhe é próprio. */
    public abstract String getDados();

    /** Em qual cadastro esta pessoa está; cada subclasse responde por si. */
    public abstract TipoCadastro getTipoCadastro();

    @Override
    public String toString() {
        return getDados();
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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
