package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaVantagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FichaVantagemRepository extends JpaRepository<FichaVantagem, Long> {

    List<FichaVantagem> findByFichaId(Long fichaId);

    Optional<FichaVantagem> findByFichaIdAndVantagemConfigId(Long fichaId, Long vantagemConfigId);
}
