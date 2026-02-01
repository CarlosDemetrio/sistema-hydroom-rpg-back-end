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
     * Busca todos os participantes de um jogo.
     */
    List<JogoParticipante> findByJogoId(Long jogoId);

    /**
     * Busca todos os participantes ativos de um jogo.
     */
    List<JogoParticipante> findByJogoIdAndAtivoTrue(Long jogoId);

    /**
     * Busca todos os jogos em que um usuário participa.
     */
    List<JogoParticipante> findByUsuarioId(Long usuarioId);

    /**
     * Busca participação específica de um usuário em um jogo (apenas ativas).
     */
    Optional<JogoParticipante> findByJogoIdAndUsuarioIdAndAtivoTrue(Long jogoId, Long usuarioId);

    /**
     * Verifica se um usuário tem uma role específica em um jogo.
     */
    boolean existsByJogoIdAndUsuarioIdAndRoleAndAtivoTrue(Long jogoId, Long usuarioId, RoleJogo role);

    /**
     * Busca participantes por jogo e role.
     */
    List<JogoParticipante> findByJogoIdAndRoleAndAtivoTrue(Long jogoId, RoleJogo role);

    /**
     * Verifica se um usuário já participa de um jogo.
     */
    boolean existsByJogoIdAndUsuarioIdAndAtivoTrue(Long jogoId, Long usuarioId);

    /**
     * Busca o role de um usuário em um jogo.
     */
    @Query("""
        SELECT p.role FROM JogoParticipante p
        WHERE p.jogo.id = :jogoId
        AND p.usuario.id = :usuarioId
        AND p.ativo = true
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
        AND p.ativo = true
    """)
    Optional<JogoParticipante> findMestreByJogoId(@Param("jogoId") Long jogoId);

    /**
     * Conta participantes ativos em um jogo.
     */
    long countByJogoIdAndAtivoTrue(Long jogoId);
}
