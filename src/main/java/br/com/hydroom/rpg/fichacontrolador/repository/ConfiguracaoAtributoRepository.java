package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.SiglaEmUsoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar configurações de atributos.
 */
@Repository
public interface ConfiguracaoAtributoRepository extends JpaRepository<AtributoConfig, Long> {

    /**
     * Busca todos os atributos ativos (não deletados) de um jogo, ordenados por ordem de exibição.
     */
    @Query("SELECT a FROM AtributoConfig a WHERE a.jogo.id = :jogoId AND a.deletedAt IS NULL ORDER BY a.ordemExibicao")
    List<AtributoConfig> findByJogoIdOrderByOrdemExibicao(Long jogoId);

    /**
     * Busca atributo por nome em um jogo (case-insensitive).
     */
    @Query("SELECT a FROM AtributoConfig a WHERE a.jogo.id = :jogoId AND LOWER(a.nome) = LOWER(:nome)")
    AtributoConfig findByJogoIdAndNomeIgnoreCase(Long jogoId, String nome);

    /**
     * Verifica se existe atributo com o nome em um jogo (case-insensitive).
     */
    @Query("SELECT COUNT(a) > 0 FROM AtributoConfig a WHERE a.jogo.id = :jogoId AND LOWER(a.nome) = LOWER(:nome)")
    boolean existsByJogoIdAndNomeIgnoreCase(Long jogoId, String nome);

    /**
     * Conta quantos atributos um jogo possui.
     * Usado para verificar se o jogo já foi inicializado.
     */
    long countByJogoId(Long jogoId);

    /**
     * Verifica se existe atributo com a abreviação no jogo, excluindo o próprio registro (para updates).
     */
    boolean existsByJogoIdAndAbreviacaoIgnoreCaseAndIdNot(Long jogoId, String abreviacao, Long id);

    /**
     * Verifica se existe atributo com a abreviação no jogo.
     */
    boolean existsByJogoIdAndAbreviacaoIgnoreCase(Long jogoId, String abreviacao);

    /**
     * Retorna todas as abreviações não-nulas de um jogo.
     */
    @Query("SELECT a.abreviacao FROM AtributoConfig a WHERE a.jogo.id = :jogoId AND a.abreviacao IS NOT NULL AND a.deletedAt IS NULL")
    List<String> findAbreviacoesByJogoId(@Param("jogoId") Long jogoId);

    /**
     * Retorna siglas de atributos com info de entidade para listagem cross-entity.
     */
    @Query("SELECT new br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.SiglaEmUsoResponse(" +
           "'ATRIBUTO', a.abreviacao, a.id, a.nome) " +
           "FROM AtributoConfig a WHERE a.jogo.id = :jogoId AND a.abreviacao IS NOT NULL AND a.deletedAt IS NULL")
    List<SiglaEmUsoResponse> findSiglasComInfoByJogoId(@Param("jogoId") Long jogoId);
}
