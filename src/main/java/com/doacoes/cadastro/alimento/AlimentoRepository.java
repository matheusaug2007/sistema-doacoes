package com.doacoes.cadastro.alimento;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlimentoRepository extends JpaRepository<Alimento, Long> {

    Optional<Alimento> findByIdAndInstituicaoId(Long id, Long instituicaoId);

    List<Alimento> findByInstituicaoIdOrderByNome(Long instituicaoId);

    @Query("""
            select a from Alimento a
            where a.instituicao.id = :instituicaoId
              and (:ativo is null or a.ativo = :ativo)
              and lower(a.nome) like :padrao
            """)
    Page<Alimento> buscar(@Param("instituicaoId") Long instituicaoId, @Param("ativo") Boolean ativo,
            @Param("padrao") String padrao, Pageable pageable);

    /** Há outro alimento (que não o de id informado) com este nome, sem diferenciar maiúsculas? */
    @Query("""
            select case when count(a) > 0 then true else false end from Alimento a
            where a.instituicao.id = :instituicaoId
              and lower(a.nome) = :nomeMinusculo
              and (:id is null or a.id <> :id)
            """)
    boolean existeOutroComNome(@Param("instituicaoId") Long instituicaoId,
            @Param("nomeMinusculo") String nomeMinusculo, @Param("id") Long id);

    @Query("select case when count(l) > 0 then true else false end from ItemDoacao l where l.alimento.id = :id")
    boolean possuiLotes(@Param("id") Long id);
}
