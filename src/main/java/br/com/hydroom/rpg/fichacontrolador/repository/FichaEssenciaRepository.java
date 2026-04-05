package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaEssencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FichaEssenciaRepository extends JpaRepository<FichaEssencia, Long> {

    Optional<FichaEssencia> findByFichaId(Long fichaId);
}
