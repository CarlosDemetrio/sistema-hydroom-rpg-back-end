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
}
