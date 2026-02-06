package br.com.hydroom.rpg.fichacontrolador.repository;

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
     * Busca todos os atributos não deletados de um jogo, ordenados por ordem de exibição.
     */
    @Query("SELECT a FROM AtributoConfig a WHERE a.jogo.id = :jogoId AND a.deletedAt IS NULL ORDER BY a.ordemExibicao")
    List<AtributoConfig> findByJogoIdAndAtivoTrueOrderByOrdemExibicao(@Param("jogoId") Long jogoId);

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

    @Query("SELECT a FROM AtributoConfig a WHERE a.deletedAt IS NULL ORDER BY a.ordemExibicao")
    List<AtributoConfig> findByAtivoTrueOrderByOrdemExibicao();
}
