package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.SiglaEmUsoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para configuração de vantagens.
 */
@Repository
public interface VantagemConfigRepository extends JpaRepository<VantagemConfig, Long> {

    /**
     * Busca todas as vantagens ativas de um jogo, ordenadas.
     */
    List<VantagemConfig> findByJogoIdOrderByOrdemExibicao(Long jogoId);

    /**
     * Verifica se uma vantagem com o nome já existe no jogo.
     */
    boolean existsByJogoIdAndNomeIgnoreCase(Long jogoId, String nome);

    /**
     * Verifica se existe vantagem com a sigla no jogo, excluindo o próprio registro (para updates).
     */
    boolean existsByJogoIdAndSiglaIgnoreCaseAndIdNot(Long jogoId, String sigla, Long id);

    /**
     * Verifica se existe vantagem com a sigla no jogo.
     */
    boolean existsByJogoIdAndSiglaIgnoreCase(Long jogoId, String sigla);

    /**
     * Retorna siglas de vantagens com info de entidade para listagem cross-entity.
     */
    @Query("SELECT new br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.SiglaEmUsoResponse(" +
           "'VANTAGEM', v.sigla, v.id, v.nome) " +
           "FROM VantagemConfig v WHERE v.jogo.id = :jogoId AND v.sigla IS NOT NULL AND v.deletedAt IS NULL")
    List<SiglaEmUsoResponse> findSiglasComInfoByJogoId(@Param("jogoId") Long jogoId);
}
