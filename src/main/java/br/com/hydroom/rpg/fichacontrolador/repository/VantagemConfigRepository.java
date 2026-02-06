package br.com.hydroom.rpg.fichacontrolador.repository;

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
     * Busca todas as vantagens ativas ordenadas por nome.
     */
    @Query("SELECT v FROM VantagemConfig v WHERE v.deletedAt IS NULL ORDER BY v.nome")
    List<VantagemConfig> findByAtivoTrueOrderByNome();

    /**
     * Busca todas as vantagens ativas de um jogo, ordenadas.
     */
    @Query("SELECT v FROM VantagemConfig v WHERE v.jogo.id = :jogoId AND v.deletedAt IS NULL ORDER BY v.ordemExibicao")
    List<VantagemConfig> findByJogoIdAndAtivoTrueOrderByOrdemExibicao(@Param("jogoId") Long jogoId);

    /**
     * Verifica se uma vantagem com o nome já existe no jogo.
     */
    boolean existsByJogoIdAndNome(Long jogoId, String nome);
}
