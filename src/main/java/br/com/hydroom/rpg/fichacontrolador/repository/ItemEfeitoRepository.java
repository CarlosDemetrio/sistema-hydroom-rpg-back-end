package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.ItemEfeito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar efeitos de itens.
 */
@Repository
public interface ItemEfeitoRepository extends JpaRepository<ItemEfeito, Long> {

    List<ItemEfeito> findByItemConfigId(Long itemConfigId);
}
