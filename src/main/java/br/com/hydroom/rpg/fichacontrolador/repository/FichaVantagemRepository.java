package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaVantagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FichaVantagemRepository extends JpaRepository<FichaVantagem, Long> {

    List<FichaVantagem> findByFichaId(Long fichaId);

    Optional<FichaVantagem> findByFichaIdAndVantagemConfigId(Long fichaId, Long vantagemConfigId);

    /**
     * Busca vantagens da ficha com JOIN FETCH no VantagemConfig e LEFT JOIN FETCH na CategoriaVantagem para evitar N+1.
     * LEFT JOIN porque categoriaVantagem é nullable.
     */
    @Query("SELECT fv FROM FichaVantagem fv JOIN FETCH fv.vantagemConfig vc LEFT JOIN FETCH vc.categoriaVantagem WHERE fv.ficha.id = :fichaId AND fv.deletedAt IS NULL")
    List<FichaVantagem> findByFichaIdWithConfig(@Param("fichaId") Long fichaId);

    /**
     * Busca vantagens com JOIN FETCH no VantagemConfig e seus efeitos.
     * Necessário para o FichaCalculationService processar efeitos sem N+1.
     *
     * <p>Atenção: efeitos é uma List, não Set — não adicionar outros JOIN FETCH
     * na mesma query para evitar MultipleBagFetchException.
     * Usar query separada se necessário buscar categoriaVantagem ou preRequisitos também.</p>
     */
    @Query("""
        SELECT fv FROM FichaVantagem fv
        JOIN FETCH fv.vantagemConfig vc
        LEFT JOIN FETCH vc.efeitos e
        LEFT JOIN FETCH e.atributoAlvo
        LEFT JOIN FETCH e.aptidaoAlvo
        LEFT JOIN FETCH e.bonusAlvo
        LEFT JOIN FETCH e.membroAlvo
        WHERE fv.ficha.id = :fichaId
        AND fv.deletedAt IS NULL
        """)
    List<FichaVantagem> findByFichaIdWithEfeitos(@Param("fichaId") Long fichaId);
}
