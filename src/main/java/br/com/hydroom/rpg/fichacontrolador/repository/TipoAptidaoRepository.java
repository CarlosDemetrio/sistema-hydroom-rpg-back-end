package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.TipoAptidao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar tipos de aptidão.
 */
@Repository
public interface TipoAptidaoRepository extends JpaRepository<TipoAptidao, Long> {

    /**
     * Busca todos os tipos de aptidão ativos de um jogo ordenados por ordem de exibição.
     */
    @Query("SELECT t FROM TipoAptidao t WHERE t.jogo.id = :jogoId AND t.deletedAt IS NULL ORDER BY t.ordemExibicao")
    List<TipoAptidao> findByJogoIdAndAtivoTrueOrderByOrdemExibicao(@Param("jogoId") Long jogoId);
}
