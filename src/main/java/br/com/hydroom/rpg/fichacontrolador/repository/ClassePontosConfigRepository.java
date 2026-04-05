package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.ClassePontosConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassePontosConfigRepository extends JpaRepository<ClassePontosConfig, Long> {

    List<ClassePontosConfig> findByClassePersonagemIdOrderByNivel(Long classeId);

    List<ClassePontosConfig> findByClassePersonagemIdAndNivelLessThanEqual(Long classeId, int nivel);

    Optional<ClassePontosConfig> findByClassePersonagemIdAndNivel(Long classeId, int nivel);

    boolean existsByClassePersonagemIdAndNivel(Long classeId, int nivel);
}
