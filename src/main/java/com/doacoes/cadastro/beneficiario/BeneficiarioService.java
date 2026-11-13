package com.doacoes.cadastro.beneficiario;

import com.doacoes.cadastro.instituicao.InstituicaoAtual;
import com.doacoes.shared.Pagina;
import com.doacoes.shared.Textos;
import com.doacoes.shared.erro.ConflitoException;
import com.doacoes.shared.erro.RecursoNaoEncontradoException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BeneficiarioService {

    private final BeneficiarioRepository repository;
    private final InstituicaoAtual instituicaoAtual;

    public BeneficiarioService(BeneficiarioRepository repository, InstituicaoAtual instituicaoAtual) {
        this.repository = repository;
        this.instituicaoAtual = instituicaoAtual;
    }

    @Transactional(readOnly = true)
    public Pagina<BeneficiarioResponse> listar(Boolean ativo, String busca, Pageable pageable) {
        return Pagina.de(
                repository.buscar(instituicaoAtual.id(), ativo, Textos.padraoDeBusca(busca), pageable),
                BeneficiarioResponse::de);
    }

    @Transactional(readOnly = true)
    public BeneficiarioResponse detalhar(Long id) {
        return BeneficiarioResponse.de(obter(id));
    }

    public BeneficiarioResponse criar(BeneficiarioRequest dados) {
        Beneficiario beneficiario = new Beneficiario(instituicaoAtual.referencia());
        aplicar(beneficiario, dados);
        return BeneficiarioResponse.de(repository.save(beneficiario));
    }

    public BeneficiarioResponse atualizar(Long id, BeneficiarioRequest dados) {
        Beneficiario beneficiario = obter(id);
        aplicar(beneficiario, dados);
        return BeneficiarioResponse.de(beneficiario);
    }

    public void desativar(Long id) {
        obter(id).setAtivo(false);
    }

    public BeneficiarioResponse reativar(Long id) {
        Beneficiario beneficiario = obter(id);
        beneficiario.setAtivo(true);
        return BeneficiarioResponse.de(beneficiario);
    }

    /** Para os outros módulos: devolve a entidade ou falha com 404. */
    @Transactional(readOnly = true)
    public Beneficiario obter(Long id) {
        return repository.findByIdAndInstituicaoId(id, instituicaoAtual.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiário não encontrado: id " + id));
    }

    private void aplicar(Beneficiario beneficiario, BeneficiarioRequest dados) {
        String documento = Textos.limpar(dados.documento());
        if (documento != null
                && repository.existeOutroComDocumento(instituicaoAtual.id(), documento, beneficiario.getId())) {
            throw new ConflitoException("Já existe um beneficiário com o documento " + documento + ".");
        }
        beneficiario.setNome(dados.nome().strip());
        beneficiario.setTipo(dados.tipo());
        beneficiario.setDocumento(documento);
        beneficiario.setTelefone(Textos.limpar(dados.telefone()));
        beneficiario.setEmail(Textos.limpar(dados.email()));
        beneficiario.setEndereco(Textos.limpar(dados.endereco()));
        beneficiario.setQuantidadePessoas(dados.quantidadePessoas() != null ? dados.quantidadePessoas() : 1);
        beneficiario.setObservacoes(Textos.limpar(dados.observacoes()));
    }
}
