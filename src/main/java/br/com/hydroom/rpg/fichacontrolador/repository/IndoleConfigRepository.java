package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.IndoleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar configurações de indoles/alinhamentos.
 */
@Repository
public interface IndoleConfigRepository extends JpaRepository<IndoleConfig, Long> {

    /**
     * Busca todas as indoles ativas de um jogo ordenadas por ordem de exibição.
     */
    List<IndoleConfig> findByJogoIdOrderByOrdemExibicao(Long jogoId);
}
