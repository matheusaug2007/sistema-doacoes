package com.doacoes.estoque;

import com.doacoes.doacao.ItemDoacao;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Consultas de lotes (ItemDoacao) vistas pelo estoque. Os métodos "travar" usam
 * SELECT ... FOR UPDATE: quem chegar depois espera a transação atual terminar e só então lê
 * o saldo, o que impede duas saídas simultâneas de gastarem a mesma quantidade.
 */
public interface LoteRepository extends JpaRepository<ItemDoacao, Long> {

    /** Lotes de um alimento que podem ser distribuídos na data, em ordem FEFO: vence primeiro, sai primeiro. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select l from ItemDoacao l
            where l.alimento.id = :alimentoId
              and l.quantidadeDisponivel > 0
              and (l.dataValidade is null or l.dataValidade >= :dataReferencia)
            order by l.dataValidade asc nulls last, l.id asc
            """)
    List<ItemDoacao> travarDisponiveis(@Param("alimentoId") Long alimentoId,
            @Param("dataReferencia") LocalDate dataReferencia);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from ItemDoacao l where l.doacao.id = :doacaoId order by l.id")
    List<ItemDoacao> travarPorDoacao(@Param("doacaoId") Long doacaoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from ItemDoacao l where l.id = :id")
    Optional<ItemDoacao> travarPorId(@Param("id") Long id);

    /** Todos os lotes com saldo da instituição, vencidos ou não, do que vence antes para o que vence depois. */
    @Query("""
            select l from ItemDoacao l
            join fetch l.alimento a
            join fetch l.doacao
            where a.instituicao.id = :instituicaoId
              and l.quantidadeDisponivel > 0
            order by l.dataValidade asc nulls last, l.id asc
            """)
    List<ItemDoacao> buscarComSaldo(@Param("instituicaoId") Long instituicaoId);
}
