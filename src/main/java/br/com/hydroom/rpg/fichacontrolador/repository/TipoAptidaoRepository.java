package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.TipoAptidao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    List<TipoAptidao> findByJogoIdOrderByOrdemExibicao(Long jogoId);

    /**
     * Verifica se existe tipo de aptidão com o nome em um jogo (case-insensitive).
     */
    @Query("SELECT COUNT(t) > 0 FROM TipoAptidao t WHERE t.jogo.id = :jogoId AND LOWER(t.nome) = LOWER(:nome)")
    boolean existsByJogoIdAndNomeIgnoreCase(Long jogoId, String nome);

    List<TipoAptidao> findByJogoIdAndNomeContainingIgnoreCaseOrderByOrdemExibicao(Long jogoId, String nome);
}
