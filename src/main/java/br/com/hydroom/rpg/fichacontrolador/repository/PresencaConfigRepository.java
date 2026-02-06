package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.PresencaConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar configurações de presença.
 */
@Repository
public interface PresencaConfigRepository extends JpaRepository<PresencaConfig, Long> {

    /**
     * Busca todas as presenças ativas de um jogo ordenadas por ordem de exibição.
     */
    @Query("SELECT p FROM PresencaConfig p WHERE p.jogo.id = :jogoId AND p.deletedAt IS NULL ORDER BY p.ordem")
    List<PresencaConfig> findByJogoIdAndAtivoTrueOrderByOrdem(@Param("jogoId") Long jogoId);
}
