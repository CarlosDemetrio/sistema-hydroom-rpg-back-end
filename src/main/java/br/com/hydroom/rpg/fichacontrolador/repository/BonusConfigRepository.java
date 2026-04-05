package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.SiglaEmUsoResponse;
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
    List<BonusConfig> findByJogoIdOrderByOrdemExibicao(Long jogoId);

    boolean existsByJogoIdAndNomeIgnoreCase(Long jogoId, String nome);

    /**
     * Verifica se existe bônus com a sigla no jogo, excluindo o próprio registro (para updates).
     */
    boolean existsByJogoIdAndSiglaIgnoreCaseAndIdNot(Long jogoId, String sigla, Long id);

    /**
     * Verifica se existe bônus com a sigla no jogo.
     */
    boolean existsByJogoIdAndSiglaIgnoreCase(Long jogoId, String sigla);

    /**
     * Retorna siglas de bônus com info de entidade para listagem cross-entity.
     */
    @Query("SELECT new br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.SiglaEmUsoResponse(" +
           "'BONUS', b.sigla, b.id, b.nome) " +
           "FROM BonusConfig b WHERE b.jogo.id = :jogoId AND b.sigla IS NOT NULL AND b.deletedAt IS NULL")
    List<SiglaEmUsoResponse> findSiglasComInfoByJogoId(@Param("jogoId") Long jogoId);

    @Query("SELECT b FROM BonusConfig b WHERE b.jogo.id = :jogoId AND LOWER(b.nome) LIKE LOWER(CONCAT('%', :nome, '%')) AND b.deletedAt IS NULL ORDER BY b.ordemExibicao")
    List<BonusConfig> findByJogoIdAndNomeContainingIgnoreCaseOrderByOrdemExibicao(@Param("jogoId") Long jogoId, @Param("nome") String nome);
}
