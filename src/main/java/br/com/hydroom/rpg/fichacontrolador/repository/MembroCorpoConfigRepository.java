package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.MembroCorpoConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar configurações de membros do corpo.
 */
@Repository
public interface MembroCorpoConfigRepository extends JpaRepository<MembroCorpoConfig, Long> {

    /**
     * Busca todos os membros do corpo ativos de um jogo ordenados por ordem de exibição.
     */
    @Query("SELECT m FROM MembroCorpoConfig m WHERE m.jogo.id = :jogoId AND m.deletedAt IS NULL ORDER BY m.ordemExibicao")
    List<MembroCorpoConfig> findByJogoIdAndAtivoTrueOrderByOrdemExibicao(@Param("jogoId") Long jogoId);
}
