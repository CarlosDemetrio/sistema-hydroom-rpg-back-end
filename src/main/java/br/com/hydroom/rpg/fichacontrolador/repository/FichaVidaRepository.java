package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaVida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FichaVidaRepository extends JpaRepository<FichaVida, Long> {

    Optional<FichaVida> findByFichaId(Long fichaId);
}
