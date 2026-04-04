package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.RacaBonusAtributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar bônus raciais de atributos.
 *
 * @author Carlos Demétrio
 * @since 2026-02-05
 */
@Repository
public interface RacaBonusAtributoRepository extends JpaRepository<RacaBonusAtributo, Long> {

    /**
     * Busca todos os bônus de uma raça específica.
     */
    List<RacaBonusAtributo> findByRacaId(Long racaId);

    /**
     * Busca todos os bônus de uma raça com AtributoConfig carregado via JOIN FETCH.
     * Evita N+1 ao acessar rba.atributo durante o recálculo da ficha.
     */
    @Query("""
        SELECT rba FROM RacaBonusAtributo rba
        JOIN FETCH rba.atributo
        WHERE rba.raca.id = :racaId
        AND rba.deletedAt IS NULL
        """)
    List<RacaBonusAtributo> findByRacaIdWithAtributo(@Param("racaId") Long racaId);

    /**
     * Busca todos os bônus que afetam um atributo específico.
     */
    List<RacaBonusAtributo> findByAtributoId(Long atributoId);

    boolean existsByRacaIdAndAtributoId(Long racaId, Long atributoId);
}
