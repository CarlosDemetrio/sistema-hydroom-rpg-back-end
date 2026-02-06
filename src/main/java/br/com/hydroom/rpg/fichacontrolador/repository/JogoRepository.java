package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade Jogo.
 * - deleted_at: soft delete (NULL = ativo)
 * - jogoAtivo: indica qual jogo está selecionado pelo mestre
 */
@Repository
public interface JogoRepository extends JpaRepository<Jogo, Long> {

    /**
     * Busca jogos não deletados onde o usuário é participante.
     */
    @Query("""
        SELECT DISTINCT j FROM Jogo j
        JOIN j.participantes p
        WHERE p.usuario.id = :usuarioId
        AND j.deletedAt IS NULL
        AND p.deletedAt IS NULL
        ORDER BY j.jogoAtivo DESC, j.nome ASC
    """)
    List<Jogo> findByParticipantesUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Busca jogos não deletados onde o usuário é mestre.
     */
    @Query("""
        SELECT DISTINCT j FROM Jogo j
        JOIN j.participantes p
        WHERE p.usuario.id = :usuarioId
        AND p.role = 'MESTRE'
        AND j.deletedAt IS NULL
        AND p.deletedAt IS NULL
        ORDER BY j.jogoAtivo DESC, j.nome ASC
    """)
    List<Jogo> findByMestreId(@Param("usuarioId") Long usuarioId);

    /**
     * Busca o jogo selecionado (jogoAtivo=true) do mestre.
     * REGRA: Apenas 1 jogo pode ter jogoAtivo=true por mestre.
     */
    @Query("""
        SELECT j FROM Jogo j
        JOIN j.participantes p
        WHERE p.usuario.id = :mestreId
        AND p.role = 'MESTRE'
        AND j.jogoAtivo = true
        AND j.deletedAt IS NULL
        AND p.deletedAt IS NULL
    """)
    Optional<Jogo> findByMestreIdAndJogoAtivoTrue(@Param("mestreId") Long mestreId);
}
