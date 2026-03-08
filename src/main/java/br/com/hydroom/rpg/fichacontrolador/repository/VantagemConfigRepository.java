package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
