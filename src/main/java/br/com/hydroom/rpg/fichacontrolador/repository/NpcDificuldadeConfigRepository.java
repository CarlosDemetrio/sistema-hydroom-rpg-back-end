package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FocoNpc;
import br.com.hydroom.rpg.fichacontrolador.model.NpcDificuldadeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciar configurações de níveis de dificuldade de NPCs.
 */
@Repository
public interface NpcDificuldadeConfigRepository extends JpaRepository<NpcDificuldadeConfig, Long> {

    /**
     * Busca todas as configurações ativas de um jogo, ordenadas por ordem de exibição.
     */
    List<NpcDificuldadeConfig> findByJogoIdOrderByOrdemExibicao(Long jogoId);

    /**
     * Busca configurações de um jogo filtradas por foco, ordenadas por ordem de exibição.
     */
    @Query("SELECT n FROM NpcDificuldadeConfig n WHERE n.jogo.id = :jogoId AND n.foco = :foco AND n.deletedAt IS NULL ORDER BY n.ordemExibicao")
    List<NpcDificuldadeConfig> findByJogoIdAndFocoOrderByOrdemExibicao(@Param("jogoId") Long jogoId, @Param("foco") FocoNpc foco);

    /**
     * Verifica se existe configuração com o nome no jogo (case-insensitive).
     */
    @Query("SELECT COUNT(n) > 0 FROM NpcDificuldadeConfig n WHERE n.jogo.id = :jogoId AND LOWER(n.nome) = LOWER(:nome) AND n.deletedAt IS NULL")
    boolean existsByJogoIdAndNomeIgnoreCase(@Param("jogoId") Long jogoId, @Param("nome") String nome);

    /**
     * Busca configuração por ID com valores de atributo carregados (evita N+1 no mapper).
     */
    @Query("SELECT n FROM NpcDificuldadeConfig n " +
           "LEFT JOIN FETCH n.valoresAtributo va LEFT JOIN FETCH va.atributoConfig " +
           "WHERE n.id = :id AND n.deletedAt IS NULL")
    Optional<NpcDificuldadeConfig> findByIdWithValores(@Param("id") Long id);
}
