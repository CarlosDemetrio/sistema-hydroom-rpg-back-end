package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.PontosVantagemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar configurações de pontos de vantagem por nível.
 */
@Repository
public interface PontosVantagemConfigRepository extends JpaRepository<PontosVantagemConfig, Long> {

    List<PontosVantagemConfig> findByJogoIdOrderByNivel(Long jogoId);

    boolean existsByJogoIdAndNivel(Long jogoId, Integer nivel);

    boolean existsByJogoIdAndNivelAndIdNot(Long jogoId, Integer nivel, Long id);
}
