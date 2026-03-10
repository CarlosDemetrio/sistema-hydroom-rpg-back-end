package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.JogoParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade JogoParticipante.
 */
@Repository
public interface JogoParticipanteRepository extends JpaRepository<JogoParticipante, Long> {

    @Query("SELECT p FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<JogoParticipante> findByJogoId(@Param("jogoId") Long jogoId);

    List<JogoParticipante> findByUsuarioId(Long usuarioId);

    @Query("SELECT p FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.usuario.id = :usuarioId AND p.deletedAt IS NULL")
    Optional<JogoParticipante> findByJogoIdAndUsuarioId(@Param("jogoId") Long jogoId, @Param("usuarioId") Long usuarioId);

    @Query("SELECT p FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.status = :status AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<JogoParticipante> findByJogoIdAndStatus(@Param("jogoId") Long jogoId, @Param("status") StatusParticipante status);

    @Query("SELECT COUNT(p) > 0 FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.usuario.id = :usuarioId AND p.deletedAt IS NULL")
    boolean existsByJogoIdAndUsuarioId(@Param("jogoId") Long jogoId, @Param("usuarioId") Long usuarioId);

    @Query("SELECT COUNT(p) > 0 FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.usuario.id = :usuarioId AND p.role = :role AND p.deletedAt IS NULL")
    boolean existsByJogoIdAndUsuarioIdAndRole(@Param("jogoId") Long jogoId, @Param("usuarioId") Long usuarioId, @Param("role") RoleJogo role);

    @Query("SELECT p FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.role = :role AND p.deletedAt IS NULL")
    List<JogoParticipante> findByJogoIdAndRole(@Param("jogoId") Long jogoId, @Param("role") RoleJogo role);

    @Query("SELECT COUNT(p) > 0 FROM JogoParticipante p WHERE p.usuario.id = :usuarioId AND p.jogo.id = :jogoId AND p.deletedAt IS NULL")
    boolean existsByUsuarioIdAndJogoId(@Param("usuarioId") Long usuarioId, @Param("jogoId") Long jogoId);

    @Query("""
        SELECT p.role FROM JogoParticipante p
        WHERE p.jogo.id = :jogoId
        AND p.usuario.id = :usuarioId
        AND p.deletedAt IS NULL
    """)
    Optional<RoleJogo> findRoleByJogoIdAndUsuarioId(@Param("jogoId") Long jogoId, @Param("usuarioId") Long usuarioId);

    @Query("""
        SELECT p FROM JogoParticipante p
        WHERE p.jogo.id = :jogoId
        AND p.role = 'MESTRE'
        AND p.deletedAt IS NULL
    """)
    Optional<JogoParticipante> findMestreByJogoId(@Param("jogoId") Long jogoId);

    @Query("SELECT COUNT(p) FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.deletedAt IS NULL")
    long countByJogoId(@Param("jogoId") Long jogoId);

    @Query("SELECT COUNT(p) > 0 FROM JogoParticipante p WHERE p.jogo.id = :jogoId AND p.usuario.id = :usuarioId AND p.status = :status AND p.deletedAt IS NULL")
    boolean existsByJogoIdAndUsuarioIdAndStatus(@Param("jogoId") Long jogoId, @Param("usuarioId") Long usuarioId, @Param("status") StatusParticipante status);
}
