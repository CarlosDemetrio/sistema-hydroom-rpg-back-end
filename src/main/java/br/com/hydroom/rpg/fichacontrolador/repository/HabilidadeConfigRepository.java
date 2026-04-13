package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.HabilidadeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório para operações de persistência de HabilidadeConfig.
 */
@Repository
public interface HabilidadeConfigRepository extends JpaRepository<HabilidadeConfig, Long> {

    List<HabilidadeConfig> findByJogoIdOrderByOrdemExibicao(Long jogoId);

    boolean existsByJogoIdAndNomeIgnoreCase(Long jogoId, String nome);
}
