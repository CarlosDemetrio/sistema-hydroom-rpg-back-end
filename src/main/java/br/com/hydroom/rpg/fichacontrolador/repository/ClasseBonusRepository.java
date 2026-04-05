package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.ClasseBonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar bônus de classes de personagem.
 */
@Repository
public interface ClasseBonusRepository extends JpaRepository<ClasseBonus, Long> {

    List<ClasseBonus> findByClasseId(Long classeId);

    /**
     * Busca todos os ClasseBonus de uma classe específica com BonusConfig carregado via JOIN FETCH.
     * Evita N+1 ao acessar cb.bonus durante o recálculo da ficha.
     */
    @Query("""
        SELECT cb FROM ClasseBonus cb
        JOIN FETCH cb.bonus
        WHERE cb.classe.id = :classeId
        AND cb.deletedAt IS NULL
        """)
    List<ClasseBonus> findByClasseIdWithBonusConfig(@Param("classeId") Long classeId);

    boolean existsByClasseIdAndBonusId(Long classeId, Long bonusId);

    void deleteByClasseId(Long classeId);
}
