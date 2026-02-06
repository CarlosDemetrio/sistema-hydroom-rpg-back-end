package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.IndoleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar configurações de indoles/alinhamentos.
 */
@Repository
public interface IndoleConfigRepository extends JpaRepository<IndoleConfig, Long> {

    /**
     * Busca todas as indoles ativas de um jogo ordenadas por ordem de exibição.
     */
    @Query("SELECT i FROM IndoleConfig i WHERE i.jogo.id = :jogoId AND i.deletedAt IS NULL ORDER BY i.ordem")
    List<IndoleConfig> findByJogoIdAndAtivoTrueOrderByOrdem(@Param("jogoId") Long jogoId);
}
