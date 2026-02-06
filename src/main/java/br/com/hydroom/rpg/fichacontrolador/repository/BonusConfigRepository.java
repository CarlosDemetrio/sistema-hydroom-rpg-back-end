package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.BonusConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar configurações de bônus.
 */
@Repository
public interface BonusConfigRepository extends JpaRepository<BonusConfig, Long> {

    /**
     * Busca todos os bônus ativos de um jogo ordenados por ordem de exibição.
     */
    @Query("SELECT b FROM BonusConfig b WHERE b.jogo.id = :jogoId AND b.deletedAt IS NULL ORDER BY b.ordemExibicao")
    List<BonusConfig> findByJogoIdAndAtivoTrueOrderByOrdemExibicao(@Param("jogoId") Long jogoId);
}
