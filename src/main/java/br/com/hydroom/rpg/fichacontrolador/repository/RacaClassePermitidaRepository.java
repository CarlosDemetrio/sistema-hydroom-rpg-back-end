package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.RacaClassePermitida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar classes permitidas por raça.
 */
@Repository
public interface RacaClassePermitidaRepository extends JpaRepository<RacaClassePermitida, Long> {

    List<RacaClassePermitida> findByRacaId(Long racaId);

    boolean existsByRacaIdAndClasseId(Long racaId, Long classeId);

    void deleteByRacaId(Long racaId);
}
