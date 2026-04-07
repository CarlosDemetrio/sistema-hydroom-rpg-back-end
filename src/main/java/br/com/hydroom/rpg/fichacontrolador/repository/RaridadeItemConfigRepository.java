package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.RaridadeItemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar configurações de raridade de itens.
 */
@Repository
public interface RaridadeItemConfigRepository extends JpaRepository<RaridadeItemConfig, Long> {

    List<RaridadeItemConfig> findByJogoIdOrderByOrdemExibicao(Long jogoId);

    boolean existsByJogoIdAndNomeIgnoreCase(Long jogoId, String nome);

    boolean existsByJogoIdAndNomeAndIdNot(Long jogoId, String nome, Long id);

    boolean existsByJogoIdAndOrdemExibicao(Long jogoId, int ordemExibicao);
}
