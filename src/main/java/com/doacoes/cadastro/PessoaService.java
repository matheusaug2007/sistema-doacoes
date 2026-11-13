package com.doacoes.cadastro;

import com.doacoes.cadastro.beneficiario.BeneficiarioRepository;
import com.doacoes.cadastro.doador.DoadorRepository;
import com.doacoes.cadastro.instituicao.InstituicaoAtual;
import com.doacoes.shared.Textos;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PessoaService {

    private static final int LIMITE_POR_CADASTRO = 50;

    private final DoadorRepository doadorRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final InstituicaoAtual instituicaoAtual;

    public PessoaService(DoadorRepository doadorRepository, BeneficiarioRepository beneficiarioRepository,
            InstituicaoAtual instituicaoAtual) {
        this.doadorRepository = doadorRepository;
        this.beneficiarioRepository = beneficiarioRepository;
        this.instituicaoAtual = instituicaoAtual;
    }

    /**
     * Procura um nome em doadores e beneficiários de uma só vez, trazendo no máximo
     * {@value #LIMITE_POR_CADASTRO} de cada cadastro.
     *
     * Polimorfismo: depois de reunidos, doadores e beneficiários são tratados apenas como
     * Pessoa. Este método não pergunta o que cada um é; é o próprio objeto que responde de
     * que cadastro vem (getTipoCadastro) e como se descreve (getDados).
     */
    @Transactional(readOnly = true)
    public List<PessoaResumo> buscar(String busca, Boolean ativo) {
        Long instituicaoId = instituicaoAtual.id();
        String padrao = Textos.padraoDeBusca(busca);
        Pageable primeiros = PageRequest.of(0, LIMITE_POR_CADASTRO, Sort.by("nome"));

        List<Pessoa> pessoas = new ArrayList<>();
        pessoas.addAll(doadorRepository.buscar(instituicaoId, ativo, padrao, primeiros).getContent());
        pessoas.addAll(beneficiarioRepository.buscar(instituicaoId, ativo, padrao, primeiros).getContent());
        pessoas.sort(Comparator.comparing(Pessoa::getNome, String.CASE_INSENSITIVE_ORDER));

        return pessoas.stream().map(PessoaResumo::de).toList();
    }
}
