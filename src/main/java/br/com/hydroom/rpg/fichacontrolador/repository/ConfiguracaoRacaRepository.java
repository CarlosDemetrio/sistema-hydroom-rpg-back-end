package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.Raca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciar configurações de raças.
 */
@Repository
public interface ConfiguracaoRacaRepository extends JpaRepository<Raca, Long> {

    /**
     * Busca todas as raças ativas de um jogo, ordenadas por ordem de exibição.
     */
    List<Raca> findByJogoIdOrderByOrdemExibicao(Long jogoId);

    /**
     * Busca raça por nome em um jogo (case-insensitive).
     */
    @Query("SELECT r FROM Raca r WHERE r.jogo.id = :jogoId AND LOWER(r.nome) = LOWER(:nome)")
    Raca findByJogoIdAndNomeIgnoreCase(Long jogoId, String nome);

    /**
     * Verifica se existe raça com o nome em um jogo (case-insensitive).
     */
    @Query("SELECT COUNT(r) > 0 FROM Raca r WHERE r.jogo.id = :jogoId AND LOWER(r.nome) = LOWER(:nome)")
    boolean existsByJogoIdAndNomeIgnoreCase(Long jogoId, String nome);

    /**
     * Busca raça por ID com bônus de atributo e classes permitidas via JOIN FETCH.
     */
    @Query("SELECT r FROM Raca r " +
           "LEFT JOIN FETCH r.bonusAtributos ba LEFT JOIN FETCH ba.atributo " +
           "LEFT JOIN FETCH r.classesPermitidas cp LEFT JOIN FETCH cp.classe " +
           "WHERE r.id = :id AND r.deletedAt IS NULL")
    Optional<Raca> findByIdWithRelationships(@Param("id") Long id);

    List<Raca> findByJogoIdAndNomeContainingIgnoreCaseOrderByOrdemExibicao(Long jogoId, String nome);
}
