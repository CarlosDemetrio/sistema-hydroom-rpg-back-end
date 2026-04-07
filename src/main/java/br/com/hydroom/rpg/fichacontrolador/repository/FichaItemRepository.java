package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FichaItemRepository extends JpaRepository<FichaItem, Long> {

    /**
     * Busca itens da ficha com JOIN FETCH em itemConfig, tipo e raridade do item.
     * Evita N+1 sem causar MultipleBagFetchException (carrega apenas raridade/tipo, não efeitos/requisitos).
     */
    @Query("""
        SELECT fi FROM FichaItem fi
        LEFT JOIN FETCH fi.itemConfig ic
        LEFT JOIN FETCH ic.raridade
        LEFT JOIN FETCH ic.tipo
        LEFT JOIN FETCH fi.raridade
        WHERE fi.ficha.id = :fichaId
        ORDER BY fi.equipado DESC, fi.nome
        """)
    List<FichaItem> findByFichaIdWithDetails(@Param("fichaId") Long fichaId);

    List<FichaItem> findByFichaId(Long fichaId);
}
