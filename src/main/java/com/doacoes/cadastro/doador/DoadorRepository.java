package com.doacoes.cadastro.doador;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoadorRepository extends JpaRepository<Doador, Long> {

    Optional<Doador> findByIdAndInstituicaoId(Long id, Long instituicaoId);

    @Query("""
            select d from Doador d
            where d.instituicao.id = :instituicaoId
              and (:ativo is null or d.ativo = :ativo)
              and lower(d.nome) like :padrao
            """)
    Page<Doador> buscar(@Param("instituicaoId") Long instituicaoId, @Param("ativo") Boolean ativo,
            @Param("padrao") String padrao, Pageable pageable);

    /** Há outro doador (que não o de id informado) com este documento? */
    @Query("""
            select case when count(d) > 0 then true else false end from Doador d
            where d.instituicao.id = :instituicaoId
              and d.documento = :documento
              and (:id is null or d.id <> :id)
            """)
    boolean existeOutroComDocumento(@Param("instituicaoId") Long instituicaoId,
            @Param("documento") String documento, @Param("id") Long id);
}
