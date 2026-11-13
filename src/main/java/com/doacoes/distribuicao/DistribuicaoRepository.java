package com.doacoes.distribuicao;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DistribuicaoRepository extends JpaRepository<Distribuicao, Long> {

    Optional<Distribuicao> findByIdAndInstituicaoId(Long id, Long instituicaoId);

    @Query("""
            select d from Distribuicao d
            where d.instituicao.id = :instituicaoId
              and (:beneficiarioId is null or d.beneficiario.id = :beneficiarioId)
              and (:de is null or d.dataDistribuicao >= :de)
              and (:ate is null or d.dataDistribuicao <= :ate)
            """)
    Page<Distribuicao> buscar(@Param("instituicaoId") Long instituicaoId,
            @Param("beneficiarioId") Long beneficiarioId, @Param("de") LocalDate de, @Param("ate") LocalDate ate,
            Pageable pageable);
}
