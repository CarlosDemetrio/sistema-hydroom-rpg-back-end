package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.GeneroConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar configurações de gêneros.
 */
@Repository
public interface GeneroConfigRepository extends JpaRepository<GeneroConfig, Long> {

    /**
     * Busca todos os gêneros ativos de um jogo ordenados por ordem de exibição.
     */
    List<GeneroConfig> findByJogoIdOrderByOrdemExibicao(Long jogoId);

    boolean existsByJogoIdAndNomeIgnoreCase(Long jogoId, String nome);
}
