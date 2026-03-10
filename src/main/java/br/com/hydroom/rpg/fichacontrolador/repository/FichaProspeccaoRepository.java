package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaProspeccao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FichaProspeccaoRepository extends JpaRepository<FichaProspeccao, Long> {

    List<FichaProspeccao> findByFichaId(Long fichaId);

    Optional<FichaProspeccao> findByFichaIdAndDadoProspeccaoConfigId(Long fichaId, Long dadoProspeccaoConfigId);
}
