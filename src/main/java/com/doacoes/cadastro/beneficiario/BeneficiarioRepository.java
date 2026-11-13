package com.doacoes.cadastro.beneficiario;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BeneficiarioRepository extends JpaRepository<Beneficiario, Long> {

    Optional<Beneficiario> findByIdAndInstituicaoId(Long id, Long instituicaoId);

    @Query("""
            select b from Beneficiario b
            where b.instituicao.id = :instituicaoId
              and (:ativo is null or b.ativo = :ativo)
              and lower(b.nome) like :padrao
            """)
    Page<Beneficiario> buscar(@Param("instituicaoId") Long instituicaoId, @Param("ativo") Boolean ativo,
            @Param("padrao") String padrao, Pageable pageable);

    /** Há outro beneficiário (que não o de id informado) com este documento? */
    @Query("""
            select case when count(b) > 0 then true else false end from Beneficiario b
            where b.instituicao.id = :instituicaoId
              and b.documento = :documento
              and (:id is null or b.id <> :id)
            """)
    boolean existeOutroComDocumento(@Param("instituicaoId") Long instituicaoId,
            @Param("documento") String documento, @Param("id") Long id);
}
