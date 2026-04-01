package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaProspeccao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FichaProspeccaoRepository extends JpaRepository<FichaProspeccao, Long> {

    List<FichaProspeccao> findByFichaId(Long fichaId);

    Optional<FichaProspeccao> findByFichaIdAndDadoProspeccaoConfigId(Long fichaId, Long dadoProspeccaoConfigId);

    /**
     * Busca prospecções da ficha com JOIN FETCH no DadoProspeccaoConfig para evitar N+1.
     */
    @Query("SELECT fp FROM FichaProspeccao fp JOIN FETCH fp.dadoProspeccaoConfig WHERE fp.ficha.id = :fichaId")
    List<FichaProspeccao> findByFichaIdWithConfig(@Param("fichaId") Long fichaId);
}
