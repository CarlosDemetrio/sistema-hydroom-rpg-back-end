package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaVidaMembro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FichaVidaMembroRepository extends JpaRepository<FichaVidaMembro, Long> {

    List<FichaVidaMembro> findByFichaId(Long fichaId);

    Optional<FichaVidaMembro> findByFichaIdAndMembroCorpoConfigId(Long fichaId, Long membroCorpoConfigId);

    /**
     * Busca membros da ficha com JOIN FETCH no MembroCorpoConfig para evitar N+1.
     * Usar nos cenários de cálculo e preview onde membroCorpoConfig.porcentagemVida/nome são acessados.
     */
    @Query("SELECT fvm FROM FichaVidaMembro fvm JOIN FETCH fvm.membroCorpoConfig WHERE fvm.ficha.id = :fichaId AND fvm.deletedAt IS NULL")
    List<FichaVidaMembro> findByFichaIdWithConfig(@Param("fichaId") Long fichaId);
}
