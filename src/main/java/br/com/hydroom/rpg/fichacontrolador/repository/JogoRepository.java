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
 *
 * @author Carlos Demétrio
 */
@Repository
public interface JogoRepository extends JpaRepository<Jogo, Long> {

    /**
     * Busca todos os jogos não deletados.
     */
    @Query("SELECT j FROM Jogo j WHERE j.deletedAt IS NULL")
    List<Jogo> findByAtivoTrue();

    /**
     * Busca um jogo não deletado por ID.
     */
    @Query("SELECT j FROM Jogo j WHERE j.id = :id AND j.deletedAt IS NULL")
    Optional<Jogo> findByIdAndAtivoTrue(@Param("id") Long id);

    /**
     * Busca jogos em que o usuário participa (não deletados).
     */
    @Query("""
        SELECT DISTINCT j FROM Jogo j
        JOIN j.participantes p
        WHERE p.usuario.id = :usuarioId
        AND j.deletedAt IS NULL
        AND p.deletedAt IS NULL
        AND p.ativo = true
        ORDER BY j.jogoAtivo DESC, j.nome ASC
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
        AND j.deletedAt IS NULL
        AND p.deletedAt IS NULL
        AND p.ativo = true
        ORDER BY j.jogoAtivo DESC, j.nome ASC
    """)
    List<Jogo> findJogosByMestre(@Param("usuarioId") Long usuarioId);
}
