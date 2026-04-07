package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.ItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciar configurações de itens.
 */
@Repository
public interface ItemConfigRepository extends JpaRepository<ItemConfig, Long> {

    List<ItemConfig> findByJogoIdOrderByOrdemExibicao(Long jogoId);

    boolean existsByJogoIdAndNomeIgnoreCase(Long jogoId, String nome);

    boolean existsByJogoIdAndNomeIgnoreCaseAndIdNot(Long jogoId, String nome, Long id);

    /**
     * Busca item por ID carregando efeitos via JOIN FETCH para evitar N+1.
     * Requisitos são carregados separadamente via Hibernate.initialize.
     */
    @Query("""
        SELECT DISTINCT i FROM ItemConfig i
        LEFT JOIN FETCH i.efeitos
        WHERE i.id = :id
        """)
    Optional<ItemConfig> findByIdWithEfeitos(@Param("id") Long id);

    /**
     * Listagem paginada com filtros opcionais por nome, raridade e categoria.
     */
    @Query("""
        SELECT i FROM ItemConfig i
        JOIN FETCH i.raridade r
        JOIN FETCH i.tipo t
        WHERE i.jogo.id = :jogoId
        AND (:nomeQuery IS NULL OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :nomeQuery, '%')))
        AND (:raridadeId IS NULL OR r.id = :raridadeId)
        AND (:categoriaItem IS NULL OR t.categoria = :categoriaItem)
        ORDER BY t.categoria, i.ordemExibicao
        """)
    Page<ItemConfig> findByJogoIdWithFilters(
        @Param("jogoId") Long jogoId,
        @Param("nomeQuery") String nomeQuery,
        @Param("raridadeId") Long raridadeId,
        @Param("categoriaItem") CategoriaItem categoriaItem,
        Pageable pageable);
    /**
     * Deleta fisicamente TODOS os ItemConfig (incluindo soft-deleted) de um jogo.
     * Usado apenas em testes para garantir limpeza completa de FKs.
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM item_configs WHERE jogo_id = :jogoId")
    void deleteAllByJogoIdNative(@Param("jogoId") Long jogoId);
}
