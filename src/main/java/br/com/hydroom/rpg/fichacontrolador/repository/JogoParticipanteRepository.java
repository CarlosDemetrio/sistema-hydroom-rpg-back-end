package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.JogoParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade JogoParticipante.
 *
 * @author Carlos Demétrio
 */
@Repository
public interface JogoParticipanteRepository extends JpaRepository<JogoParticipante, Long> {

    /**
     * Busca todos os participantes ativos (não deletados) de um jogo.
     */
    @Query("SELECT p FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.deletedAt IS NULL")
    List<JogoParticipante> findByJogoId(@Param("jogoId") Long jogoId);

    /**
     * Busca todos os jogos em que um usuário participa.
     */
    List<JogoParticipante> findByUsuarioId(Long usuarioId);

    /**
     * Busca participação específica de um usuário em um jogo (apenas ativas).
     */
    @Query("SELECT p FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.usuario.id = :usuarioId AND p.deletedAt IS NULL")
    Optional<JogoParticipante> findByJogoIdAndUsuarioId(@Param("jogoId") Long jogoId, @Param("usuarioId") Long usuarioId);

    /**
     * Verifica se um usuário tem uma role específica em um jogo.
     */
    @Query("SELECT COUNT(p) > 0 FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.usuario.id = :usuarioId AND p.role = :role AND p.deletedAt IS NULL")
    boolean existsByJogoIdAndUsuarioIdAndRole(@Param("jogoId") Long jogoId, @Param("usuarioId") Long usuarioId, @Param("role") RoleJogo role);

    /**
     * Busca participantes por jogo e role.
     */
    @Query("SELECT p FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.role = :role AND p.deletedAt IS NULL")
    List<JogoParticipante> findByJogoIdAndRole(@Param("jogoId") Long jogoId, @Param("role") RoleJogo role);

    /**
     * Verifica se usuário participa do jogo (qualquer role).
     */
    @Query("SELECT COUNT(p) > 0 FROM JogoParticipante p WHERE p.usuario.id = :usuarioId AND p.jogo.id = :jogoId AND p.deletedAt IS NULL")
    boolean existsByUsuarioIdAndJogoId(@Param("usuarioId") Long usuarioId, @Param("jogoId") Long jogoId);

    /**
     * Busca o role de um usuário em um jogo.
     */
    @Query("""
        SELECT p.role FROM JogoParticipante p
        WHERE p.jogo.id = :jogoId
        AND p.usuario.id = :usuarioId
        AND p.deletedAt IS NULL
    """)
    Optional<RoleJogo> findRoleByJogoIdAndUsuarioId(
        @Param("jogoId") Long jogoId,
        @Param("usuarioId") Long usuarioId
    );

    /**
     * Busca o mestre de um jogo.
     */
    @Query("""
        SELECT p FROM JogoParticipante p
        WHERE p.jogo.id = :jogoId
        AND p.role = 'MESTRE'
        AND p.deletedAt IS NULL
    """)
    Optional<JogoParticipante> findMestreByJogoId(@Param("jogoId") Long jogoId);

    /**
     * Conta participantes ativos em um jogo.
     */
    @Query("SELECT COUNT(p) FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.deletedAt IS NULL")
    long countByJogoId(@Param("jogoId") Long jogoId);
}
