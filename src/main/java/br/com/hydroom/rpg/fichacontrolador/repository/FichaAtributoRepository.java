package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaAtributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FichaAtributoRepository extends JpaRepository<FichaAtributo, Long> {

    List<FichaAtributo> findByFichaId(Long fichaId);

    Optional<FichaAtributo> findByFichaIdAndAtributoConfigId(Long fichaId, Long atributoConfigId);
}
