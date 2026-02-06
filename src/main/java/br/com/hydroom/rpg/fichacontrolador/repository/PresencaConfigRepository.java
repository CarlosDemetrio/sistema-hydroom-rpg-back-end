package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.PresencaConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar configurações de presença.
 */
@Repository
public interface PresencaConfigRepository extends JpaRepository<PresencaConfig, Long> {

    /**
     * Busca todas as presenças ativas de um jogo ordenadas por ordem de exibição.
     */
    List<PresencaConfig> findByJogoIdOrderByOrdem(Long jogoId);
}
