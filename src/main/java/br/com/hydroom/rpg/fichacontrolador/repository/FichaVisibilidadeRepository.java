package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaVisibilidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FichaVisibilidadeRepository extends JpaRepository<FichaVisibilidade, Long> {

    List<FichaVisibilidade> findByFichaId(Long fichaId);

    Optional<FichaVisibilidade> findByFichaIdAndJogadorId(Long fichaId, Long jogadorId);

    boolean existsByFichaIdAndJogadorId(Long fichaId, Long jogadorId);

    /**
     * Busca IDs dos jogadores com acesso aos stats de um NPC específico.
     */
    @Query("SELECT fv.jogadorId FROM FichaVisibilidade fv WHERE fv.ficha.id = :fichaId AND fv.deletedAt IS NULL")
    List<Long> findJogadorIdsByFichaId(@Param("fichaId") Long fichaId);
}
