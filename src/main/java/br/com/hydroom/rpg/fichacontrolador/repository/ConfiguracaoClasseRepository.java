package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciar configurações de classes.
 */
@Repository
public interface ConfiguracaoClasseRepository extends JpaRepository<ClassePersonagem, Long> {

    /**
     * Busca todas as classes ativas de um jogo, ordenadas por ordem de exibição.
     */
    List<ClassePersonagem> findByJogoIdOrderByOrdemExibicao(Long jogoId);

    /**
     * Busca classe por nome em um jogo (case-insensitive).
     */
    @Query("SELECT c FROM ClassePersonagem c WHERE c.jogo.id = :jogoId AND LOWER(c.nome) = LOWER(:nome)")
    ClassePersonagem findByJogoIdAndNomeIgnoreCase(Long jogoId, String nome);

    /**
     * Verifica se existe classe com o nome em um jogo (case-insensitive).
     */
    @Query("SELECT COUNT(c) > 0 FROM ClassePersonagem c WHERE c.jogo.id = :jogoId AND LOWER(c.nome) = LOWER(:nome)")
    boolean existsByJogoIdAndNomeIgnoreCase(Long jogoId, String nome);

    /**
     * Busca classe por ID com listas de bônus carregadas (evita N+1 no mapper).
     */
    @Query("SELECT c FROM ClassePersonagem c " +
           "LEFT JOIN FETCH c.bonusConfig cb LEFT JOIN FETCH cb.bonus " +
           "LEFT JOIN FETCH c.aptidaoBonus ab LEFT JOIN FETCH ab.aptidao " +
           "WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<ClassePersonagem> findByIdWithBonuses(@Param("id") Long id);
}
