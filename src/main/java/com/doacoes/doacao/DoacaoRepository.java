package com.doacoes.doacao;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoacaoRepository extends JpaRepository<Doacao, Long> {

    Optional<Doacao> findByIdAndInstituicaoId(Long id, Long instituicaoId);

    @Query("""
            select d from Doacao d
            where d.instituicao.id = :instituicaoId
              and (:doadorId is null or d.doador.id = :doadorId)
              and (:de is null or d.dataRecebimento >= :de)
              and (:ate is null or d.dataRecebimento <= :ate)
            """)
    Page<Doacao> buscar(@Param("instituicaoId") Long instituicaoId, @Param("doadorId") Long doadorId,
            @Param("de") LocalDate de, @Param("ate") LocalDate ate, Pageable pageable);
}
