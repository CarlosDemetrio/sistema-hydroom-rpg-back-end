package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaAmeaca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FichaAmeacaRepository extends JpaRepository<FichaAmeaca, Long> {

    Optional<FichaAmeaca> findByFichaId(Long fichaId);
}
