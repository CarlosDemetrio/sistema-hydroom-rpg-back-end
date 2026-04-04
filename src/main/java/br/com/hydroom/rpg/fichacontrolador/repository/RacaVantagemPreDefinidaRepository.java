package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.RacaVantagemPreDefinida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RacaVantagemPreDefinidaRepository extends JpaRepository<RacaVantagemPreDefinida, Long> {

    List<RacaVantagemPreDefinida> findByRacaIdOrderByNivel(Long racaId);

    List<RacaVantagemPreDefinida> findByRacaIdAndNivel(Long racaId, int nivel);

    @Query("""
        SELECT rvp FROM RacaVantagemPreDefinida rvp
        JOIN FETCH rvp.vantagemConfig
        WHERE rvp.raca.id = :racaId
        AND rvp.nivel = :nivel
        AND rvp.deletedAt IS NULL
        """)
    List<RacaVantagemPreDefinida> findByRacaIdAndNivelWithVantagem(
        @Param("racaId") Long racaId,
        @Param("nivel") int nivel);

    boolean existsByRacaIdAndNivelAndVantagemConfigId(
        Long racaId, int nivel, Long vantagemConfigId);
}
