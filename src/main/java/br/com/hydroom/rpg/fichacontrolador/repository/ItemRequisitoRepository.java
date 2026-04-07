package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.ItemRequisito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar requisitos de itens.
 */
@Repository
public interface ItemRequisitoRepository extends JpaRepository<ItemRequisito, Long> {

    List<ItemRequisito> findByItemConfigId(Long itemConfigId);
}
