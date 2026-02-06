package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.Raca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

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

}
