package com.doacoes.estoque;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimentacaoRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByLoteIdOrderById(Long loteId);

    /** O período é fechado no início e aberto no fim: de &lt;= criadoEm &lt; ate. */
    @Query("""
            select m from MovimentacaoEstoque m
            join m.lote l
            join l.alimento a
            where a.instituicao.id = :instituicaoId
              and (:alimentoId is null or a.id = :alimentoId)
              and (:tipo is null or m.tipo = :tipo)
              and (:de is null or m.criadoEm >= :de)
              and (:ate is null or m.criadoEm < :ate)
            """)
    Page<MovimentacaoEstoque> buscar(@Param("instituicaoId") Long instituicaoId,
            @Param("alimentoId") Long alimentoId, @Param("tipo") TipoMovimentacao tipo,
            @Param("de") LocalDateTime de, @Param("ate") LocalDateTime ate, Pageable pageable);
}
