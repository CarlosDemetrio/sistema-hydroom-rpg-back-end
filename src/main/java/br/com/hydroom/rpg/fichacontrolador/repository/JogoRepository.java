package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para a entidade Jogo.
 *
 * @author Carlos Demétrio
 */
@Repository
public interface JogoRepository extends JpaRepository<Jogo, Long> {

    /**
     * Busca todos os jogos ativos.
     */
    List<Jogo> findByAtivoTrue();

    /**
     * Busca um jogo ativo por ID.
     */
    Optional<Jogo> findByIdAndAtivoTrue(Long id);

    /**
     * Busca jogos em que o usuário participa (ativo).
     */
    @Query("""
        SELECT DISTINCT j FROM Jogo j
        JOIN j.participantes p
        WHERE p.usuario.id = :usuarioId
        AND j.ativo = true
        AND p.ativo = true
        ORDER BY j.nome ASC
    """)
    List<Jogo> findJogosByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Busca jogos em que o usuário é mestre.
     */
    @Query("""
        SELECT DISTINCT j FROM Jogo j
        JOIN j.participantes p
        WHERE p.usuario.id = :usuarioId
        AND p.role = 'MESTRE'
        AND j.ativo = true
        AND p.ativo = true
        ORDER BY j.nome ASC
    """)
    List<Jogo> findJogosByMestre(@Param("usuarioId") Long usuarioId);

    /**
     * Busca o jogo ativo do mestre (apenas 1 jogo pode estar ativo por mestre).
     */
    @Query("""
        SELECT j FROM Jogo j
        JOIN j.participantes p
        WHERE p.usuario.id = :mestreId
        AND p.role = 'MESTRE'
        AND j.ativo = true
        AND p.ativo = true
    """)
    Optional<Jogo> findByMestreIdAndAtivoTrue(@Param("mestreId") Long mestreId);

    /**
     * Desativa todos os jogos de um mestre específico.
     * Usado ao ativar um novo jogo (apenas 1 pode estar ativo por vez).
     */
    @Modifying
    @Query("""
        UPDATE Jogo j SET j.ativo = false
        WHERE j.id IN (
            SELECT jp.jogo.id FROM JogoParticipante jp
            WHERE jp.usuario.id = :mestreId
            AND jp.role = 'MESTRE'
            AND jp.ativo = true
        )
    """)
    void desativarTodosDoMestre(@Param("mestreId") Long mestreId);
}
