package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.RacaPontosConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RacaPontosConfigRepository extends JpaRepository<RacaPontosConfig, Long> {

    List<RacaPontosConfig> findByRacaIdOrderByNivel(Long racaId);

    List<RacaPontosConfig> findByRacaIdAndNivelLessThanEqual(Long racaId, int nivel);

    Optional<RacaPontosConfig> findByRacaIdAndNivel(Long racaId, int nivel);

    boolean existsByRacaIdAndNivel(Long racaId, int nivel);
}
