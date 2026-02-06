package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.DadoProspeccaoConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para configuração de dados de prospecção.
 */
@Repository
public interface DadoProspeccaoConfigRepository extends JpaRepository<DadoProspeccaoConfig, Long> {

    /**
     * Busca todos os dados de prospecção ativos ordenados.
     */
    @Query("SELECT d FROM DadoProspeccaoConfig d WHERE d.deletedAt IS NULL ORDER BY d.ordemExibicao")
    List<DadoProspeccaoConfig> findByAtivoTrueOrderByOrdemExibicao();

    /**
     * Busca todos os dados de prospecção ativos de um jogo, ordenados.
     */
    @Query("SELECT d FROM DadoProspeccaoConfig d WHERE d.jogo.id = :jogoId AND d.deletedAt IS NULL ORDER BY d.ordemExibicao")
    List<DadoProspeccaoConfig> findByJogoIdAndAtivoTrueOrderByOrdemExibicao(@Param("jogoId") Long jogoId);

    /**
     * Busca dado de prospecção por nome em um jogo.
     */
    boolean existsByJogoIdAndNome(Long jogoId, String nome);
}
