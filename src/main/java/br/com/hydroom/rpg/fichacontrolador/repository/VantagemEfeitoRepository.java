package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.VantagemEfeito;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoEfeito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para efeitos de vantagem.
 */
@Repository
public interface VantagemEfeitoRepository extends JpaRepository<VantagemEfeito, Long> {

    List<VantagemEfeito> findByVantagemConfigId(Long vantagemConfigId);

    List<VantagemEfeito> findByVantagemConfigIdAndTipoEfeito(Long vantagemConfigId, TipoEfeito tipoEfeito);
}
