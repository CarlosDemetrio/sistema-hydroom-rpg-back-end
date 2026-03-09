package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.ClasseAptidaoBonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar bônus de aptidão de classes de personagem.
 */
@Repository
public interface ClasseAptidaoBonusRepository extends JpaRepository<ClasseAptidaoBonus, Long> {

    List<ClasseAptidaoBonus> findByClasseId(Long classeId);

    boolean existsByClasseIdAndAptidaoId(Long classeId, Long aptidaoId);

    void deleteByClasseId(Long classeId);
}
