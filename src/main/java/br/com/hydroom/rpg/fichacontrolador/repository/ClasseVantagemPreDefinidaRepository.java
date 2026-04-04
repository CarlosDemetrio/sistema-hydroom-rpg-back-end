package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.ClasseVantagemPreDefinida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClasseVantagemPreDefinidaRepository extends JpaRepository<ClasseVantagemPreDefinida, Long> {

    List<ClasseVantagemPreDefinida> findByClassePersonagemIdOrderByNivel(Long classeId);

    List<ClasseVantagemPreDefinida> findByClassePersonagemIdAndNivel(Long classeId, int nivel);

    @Query("""
        SELECT cvp FROM ClasseVantagemPreDefinida cvp
        JOIN FETCH cvp.vantagemConfig
        WHERE cvp.classePersonagem.id = :classeId
        AND cvp.nivel = :nivel
        AND cvp.deletedAt IS NULL
        """)
    List<ClasseVantagemPreDefinida> findByClasseIdAndNivelWithVantagem(
        @Param("classeId") Long classeId,
        @Param("nivel") int nivel);

    boolean existsByClassePersonagemIdAndNivelAndVantagemConfigId(
        Long classeId, int nivel, Long vantagemConfigId);
}
