package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaBonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FichaBonusRepository extends JpaRepository<FichaBonus, Long> {

    List<FichaBonus> findByFichaId(Long fichaId);

    Optional<FichaBonus> findByFichaIdAndBonusConfigId(Long fichaId, Long bonusConfigId);

    /**
     * Busca bônus da ficha com JOIN FETCH no BonusConfig para evitar N+1.
     * Usar nos cenários de cálculo e resumo onde bonusConfig.formulaBase/nome/sigla são acessados.
     */
    @Query("SELECT fb FROM FichaBonus fb JOIN FETCH fb.bonusConfig WHERE fb.ficha.id = :fichaId AND fb.deletedAt IS NULL")
    List<FichaBonus> findByFichaIdWithConfig(@Param("fichaId") Long fichaId);
}
