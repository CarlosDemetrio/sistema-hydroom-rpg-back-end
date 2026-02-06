package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.GeneroConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar configurações de gêneros.
 */
@Repository
public interface GeneroConfigRepository extends JpaRepository<GeneroConfig, Long> {

    /**
     * Busca todos os gêneros ativos de um jogo ordenados por ordem de exibição.
     */
    @Query("SELECT g FROM GeneroConfig g WHERE g.jogo.id = :jogoId AND g.deletedAt IS NULL ORDER BY g.ordem")
    List<GeneroConfig> findByJogoIdAndAtivoTrueOrderByOrdem(@Param("jogoId") Long jogoId);
}
