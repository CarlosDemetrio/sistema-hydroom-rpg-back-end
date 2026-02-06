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
     * Busca todos os participantes de um jogo (não deletados).
     */
    @Query("SELECT p FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.deletedAt IS NULL")
    List<JogoParticipante> findByJogoId(@Param("jogoId") Long jogoId);

    /**
     * Busca todos os participantes não deletados de um jogo.
     *
     * @deprecated Use {@link #findByJogoId(Long)} instead. This method maintains backward compatibility
     * with the legacy naming convention but has identical implementation. The method name suggests
     * filtering by "ativo=true" which is now replaced by the soft delete pattern (deletedAt IS NULL).
     * This method will be removed in a future version.
     */
    @Query("SELECT p FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.deletedAt IS NULL")
    List<JogoParticipante> findByJogoIdAndAtivoTrue(@Param("jogoId") Long jogoId);

    /**
     * Busca todos os jogos em que um usuário participa.
     */
    @Query("SELECT p FROM JogoParticipante p WHERE p.usuario.id = :usuarioId AND p.deletedAt IS NULL")
    List<JogoParticipante> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Busca participação específica de um usuário em um jogo (apenas não deletadas).
     */
    @Query("SELECT p FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.usuario.id = :usuarioId AND p.deletedAt IS NULL")
    Optional<JogoParticipante> findByJogoIdAndUsuarioIdAndAtivoTrue(@Param("jogoId") Long jogoId, @Param("usuarioId") Long usuarioId);

    /**
     * Verifica se um usuário tem uma role específica em um jogo.
     */
    @Query("SELECT COUNT(p) > 0 FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.usuario.id = :usuarioId AND p.role = :role AND p.deletedAt IS NULL")
    boolean existsByJogoIdAndUsuarioIdAndRoleAndAtivoTrue(@Param("jogoId") Long jogoId, @Param("usuarioId") Long usuarioId, @Param("role") RoleJogo role);

    /**
     * Busca participantes por jogo e role.
     */
    @Query("SELECT p FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.role = :role AND p.deletedAt IS NULL")
    List<JogoParticipante> findByJogoIdAndRoleAndAtivoTrue(@Param("jogoId") Long jogoId, @Param("role") RoleJogo role);

    /**
     * Verifica se usuário participa do jogo (qualquer role).
     */
    @Query("SELECT COUNT(p) > 0 FROM JogoParticipante p WHERE p.usuario.id = :usuarioId AND p.jogo.id = :jogoId AND p.deletedAt IS NULL")
    boolean existsByUsuarioIdAndJogoIdAndAtivoTrue(@Param("usuarioId") Long usuarioId, @Param("jogoId") Long jogoId);

    /**
     * Verifica se usuário tem role específica em um jogo.
     */
    @Query("SELECT COUNT(p) > 0 FROM JogoParticipante p WHERE p.usuario.id = :usuarioId AND p.jogo.id = :jogoId AND p.role = :role AND p.deletedAt IS NULL")
    boolean existsByUsuarioIdAndJogoIdAndRoleAndAtivoTrue(@Param("usuarioId") Long usuarioId, @Param("jogoId") Long jogoId, @Param("role") RoleJogo role);

    /**
     * Verifica se um usuário já participa de um jogo.
     */
    @Query("SELECT COUNT(p) > 0 FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.usuario.id = :usuarioId AND p.deletedAt IS NULL")
    boolean existsByJogoIdAndUsuarioIdAndAtivoTrue(@Param("jogoId") Long jogoId, @Param("usuarioId") Long usuarioId);

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
     * Conta participantes não deletados em um jogo.
     */
    @Query("SELECT COUNT(p) FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.deletedAt IS NULL")
    long countByJogoIdAndAtivoTrue(@Param("jogoId") Long jogoId);
}
