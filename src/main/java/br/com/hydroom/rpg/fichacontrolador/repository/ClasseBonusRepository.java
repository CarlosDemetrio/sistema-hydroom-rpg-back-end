package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.ClasseBonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar bônus de classes de personagem.
 */
@Repository
public interface ClasseBonusRepository extends JpaRepository<ClasseBonus, Long> {

    List<ClasseBonus> findByClasseId(Long classeId);

    boolean existsByClasseIdAndBonusId(Long classeId, Long bonusId);

    void deleteByClasseId(Long classeId);
}
