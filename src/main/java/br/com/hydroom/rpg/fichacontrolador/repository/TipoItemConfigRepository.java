package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.TipoItemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar configurações de tipo de itens.
 */
@Repository
public interface TipoItemConfigRepository extends JpaRepository<TipoItemConfig, Long> {

    List<TipoItemConfig> findByJogoIdOrderByOrdemExibicao(Long jogoId);

    boolean existsByJogoIdAndNomeIgnoreCase(Long jogoId, String nome);

    boolean existsByJogoIdAndNomeAndIdNot(Long jogoId, String nome, Long id);
}
