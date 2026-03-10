package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaDescricaoFisica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FichaDescricaoFisicaRepository extends JpaRepository<FichaDescricaoFisica, Long> {

    Optional<FichaDescricaoFisica> findByFichaId(Long fichaId);
}
