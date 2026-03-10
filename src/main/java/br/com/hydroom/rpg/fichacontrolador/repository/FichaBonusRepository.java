package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaBonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FichaBonusRepository extends JpaRepository<FichaBonus, Long> {

    List<FichaBonus> findByFichaId(Long fichaId);

    Optional<FichaBonus> findByFichaIdAndBonusConfigId(Long fichaId, Long bonusConfigId);
}
