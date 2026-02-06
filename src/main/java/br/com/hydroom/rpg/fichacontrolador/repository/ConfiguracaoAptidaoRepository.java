package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.AptidaoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.TipoAptidao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar configurações de aptidões.
 */
@Repository
public interface ConfiguracaoAptidaoRepository extends JpaRepository<AptidaoConfig, Long> {

    /**
     * Busca todas as aptidões ativas de um jogo, ordenadas por ordem de exibição.
     */
    @Query("SELECT a FROM AptidaoConfig a WHERE a.jogo.id = :jogoId AND a.deletedAt IS NULL ORDER BY a.ordemExibicao")
    List<AptidaoConfig> findByJogoIdAndAtivoTrueOrderByOrdemExibicao(@Param("jogoId") Long jogoId);

    /**
     * Busca aptidões por tipo em um jogo.
     */
    @Query("SELECT a FROM AptidaoConfig a WHERE a.jogo.id = :jogoId AND a.tipoAptidao = :tipoAptidao AND a.deletedAt IS NULL ORDER BY a.ordemExibicao")
    List<AptidaoConfig> findByJogoIdAndTipoAptidaoAndAtivoTrueOrderByOrdemExibicao(@Param("jogoId") Long jogoId, @Param("tipoAptidao") TipoAptidao tipoAptidao);

    /**
     * Busca aptidão por nome em um jogo (case-insensitive).
     */
    @Query("SELECT a FROM AptidaoConfig a WHERE a.jogo.id = :jogoId AND LOWER(a.nome) = LOWER(:nome)")
    AptidaoConfig findByJogoIdAndNomeIgnoreCase(Long jogoId, String nome);

    /**
     * Verifica se existe aptidão com o nome em um jogo (case-insensitive).
     */
    @Query("SELECT COUNT(a) > 0 FROM AptidaoConfig a WHERE a.jogo.id = :jogoId AND LOWER(a.nome) = LOWER(:nome)")
    boolean existsByJogoIdAndNomeIgnoreCase(Long jogoId, String nome);
}
