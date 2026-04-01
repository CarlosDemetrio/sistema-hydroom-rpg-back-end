package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FichaRepository extends JpaRepository<Ficha, Long> {

    List<Ficha> findByJogoId(Long jogoId);

    List<Ficha> findByJogoIdAndIsNpcFalseOrderByNome(Long jogoId);

    List<Ficha> findByJogoIdAndIsNpcTrue(Long jogoId);

    List<Ficha> findByJogadorId(Long jogadorId);

    List<Ficha> findByJogoIdAndJogadorId(Long jogoId, Long jogadorId);

    /**
     * Conta fichas de jogadores (não NPCs) de um jogo.
     */
    long countByJogoIdAndIsNpcFalse(Long jogoId);

    /**
     * Conta fichas de um usuário específico em um jogo (não NPCs).
     */
    long countByJogoIdAndJogadorIdAndIsNpcFalse(Long jogoId, Long jogadorId);

    /**
     * Busca fichas com filtros opcionais (nome, classeId, racaId, nivel).
     * Mestre vê todas as fichas de jogadores (isNpc=false).
     */
    @Query("SELECT f FROM Ficha f WHERE f.jogo.id = :jogoId " +
           "AND (:nome IS NULL OR LOWER(f.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) " +
           "AND (:classeId IS NULL OR f.classe.id = :classeId) " +
           "AND (:racaId IS NULL OR f.raca.id = :racaId) " +
           "AND (:nivel IS NULL OR f.nivel = :nivel) " +
           "AND f.isNpc = false " +
           "AND f.deletedAt IS NULL " +
           "ORDER BY f.nome")
    List<Ficha> findByJogoIdWithFilters(
            @Param("jogoId") Long jogoId,
            @Param("nome") String nome,
            @Param("classeId") Long classeId,
            @Param("racaId") Long racaId,
            @Param("nivel") Integer nivel);

    /**
     * Busca fichas de um jogador específico com filtros opcionais.
     */
    @Query("SELECT f FROM Ficha f WHERE f.jogo.id = :jogoId " +
           "AND f.jogadorId = :jogadorId " +
           "AND (:nome IS NULL OR LOWER(f.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) " +
           "AND (:classeId IS NULL OR f.classe.id = :classeId) " +
           "AND (:racaId IS NULL OR f.raca.id = :racaId) " +
           "AND (:nivel IS NULL OR f.nivel = :nivel) " +
           "AND f.isNpc = false " +
           "AND f.deletedAt IS NULL " +
           "ORDER BY f.nome")
    List<Ficha> findByJogoIdAndJogadorIdWithFilters(
            @Param("jogoId") Long jogoId,
            @Param("jogadorId") Long jogadorId,
            @Param("nome") String nome,
            @Param("classeId") Long classeId,
            @Param("racaId") Long racaId,
            @Param("nivel") Integer nivel);

    /**
     * Busca top 5 fichas recentemente alteradas de um jogo (não NPCs).
     */
    @Query("SELECT f FROM Ficha f WHERE f.jogo.id = :jogoId AND f.isNpc = false AND f.deletedAt IS NULL ORDER BY f.updatedAt DESC LIMIT 5")
    List<Ficha> findTop5ByJogoIdRecentlyUpdated(@Param("jogoId") Long jogoId);

    /**
     * Conta fichas agrupadas por nível para um jogo (não NPCs).
     */
    @Query("SELECT f.nivel, COUNT(f) FROM Ficha f WHERE f.jogo.id = :jogoId AND f.isNpc = false AND f.deletedAt IS NULL GROUP BY f.nivel")
    List<Object[]> countByJogoIdGroupByNivel(@Param("jogoId") Long jogoId);

    /**
     * Busca fichas de um jogador em um jogo (não NPCs, isNpc=false).
     */
    List<Ficha> findByJogoIdAndJogadorIdAndIsNpcFalse(Long jogoId, Long jogadorId);

    /**
     * Busca ficha por ID com JOIN FETCH nos relacionamentos ManyToOne para evitar N+1.
     * Usar quando o FichaMapper.toResponse() precisar acessar raca.nome, classe.nome, etc.
     */
    @Query("SELECT f FROM Ficha f " +
           "LEFT JOIN FETCH f.jogo " +
           "LEFT JOIN FETCH f.raca " +
           "LEFT JOIN FETCH f.classe " +
           "LEFT JOIN FETCH f.genero " +
           "LEFT JOIN FETCH f.indole " +
           "LEFT JOIN FETCH f.presenca " +
           "WHERE f.id = :id AND f.deletedAt IS NULL")
    Optional<Ficha> findByIdWithRelationships(@Param("id") Long id);

    /**
     * Busca fichas por jogoId com JOIN FETCH nos relacionamentos ManyToOne para evitar N+1.
     * Usar em listagens que retornam FichaResponse (com nomes das relações).
     */
    @Query("SELECT f FROM Ficha f " +
           "LEFT JOIN FETCH f.jogo " +
           "LEFT JOIN FETCH f.raca " +
           "LEFT JOIN FETCH f.classe " +
           "LEFT JOIN FETCH f.genero " +
           "LEFT JOIN FETCH f.indole " +
           "LEFT JOIN FETCH f.presenca " +
           "WHERE f.jogo.id = :jogoId " +
           "AND (:nome IS NULL OR LOWER(f.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) " +
           "AND (:classeId IS NULL OR f.classe.id = :classeId) " +
           "AND (:racaId IS NULL OR f.raca.id = :racaId) " +
           "AND (:nivel IS NULL OR f.nivel = :nivel) " +
           "AND f.isNpc = false " +
           "AND f.deletedAt IS NULL " +
           "ORDER BY f.nome")
    List<Ficha> findByJogoIdWithFiltersAndRelationships(
            @Param("jogoId") Long jogoId,
            @Param("nome") String nome,
            @Param("classeId") Long classeId,
            @Param("racaId") Long racaId,
            @Param("nivel") Integer nivel);

    /**
     * Busca fichas de um jogador com JOIN FETCH nos relacionamentos ManyToOne.
     */
    @Query("SELECT f FROM Ficha f " +
           "LEFT JOIN FETCH f.jogo " +
           "LEFT JOIN FETCH f.raca " +
           "LEFT JOIN FETCH f.classe " +
           "LEFT JOIN FETCH f.genero " +
           "LEFT JOIN FETCH f.indole " +
           "LEFT JOIN FETCH f.presenca " +
           "WHERE f.jogo.id = :jogoId " +
           "AND f.jogadorId = :jogadorId " +
           "AND (:nome IS NULL OR LOWER(f.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) " +
           "AND (:classeId IS NULL OR f.classe.id = :classeId) " +
           "AND (:racaId IS NULL OR f.raca.id = :racaId) " +
           "AND (:nivel IS NULL OR f.nivel = :nivel) " +
           "AND f.isNpc = false " +
           "AND f.deletedAt IS NULL " +
           "ORDER BY f.nome")
    List<Ficha> findByJogoIdAndJogadorIdWithFiltersAndRelationships(
            @Param("jogoId") Long jogoId,
            @Param("jogadorId") Long jogadorId,
            @Param("nome") String nome,
            @Param("classeId") Long classeId,
            @Param("racaId") Long racaId,
            @Param("nivel") Integer nivel);

    /**
     * Busca NPCs de um jogo com JOIN FETCH nos relacionamentos ManyToOne.
     */
    @Query("SELECT f FROM Ficha f " +
           "LEFT JOIN FETCH f.jogo " +
           "LEFT JOIN FETCH f.raca " +
           "LEFT JOIN FETCH f.classe " +
           "LEFT JOIN FETCH f.genero " +
           "LEFT JOIN FETCH f.indole " +
           "LEFT JOIN FETCH f.presenca " +
           "WHERE f.jogo.id = :jogoId AND f.isNpc = true AND f.deletedAt IS NULL")
    List<Ficha> findByJogoIdAndIsNpcTrueWithRelationships(@Param("jogoId") Long jogoId);

    /**
     * Busca fichas do jogador (não NPCs) com JOIN FETCH nos relacionamentos ManyToOne.
     */
    @Query("SELECT f FROM Ficha f " +
           "LEFT JOIN FETCH f.jogo " +
           "LEFT JOIN FETCH f.raca " +
           "LEFT JOIN FETCH f.classe " +
           "LEFT JOIN FETCH f.genero " +
           "LEFT JOIN FETCH f.indole " +
           "LEFT JOIN FETCH f.presenca " +
           "WHERE f.jogo.id = :jogoId AND f.jogadorId = :jogadorId AND f.isNpc = false AND f.deletedAt IS NULL")
    List<Ficha> findByJogoIdAndJogadorIdAndIsNpcFalseWithRelationships(
            @Param("jogoId") Long jogoId,
            @Param("jogadorId") Long jogadorId);
}
