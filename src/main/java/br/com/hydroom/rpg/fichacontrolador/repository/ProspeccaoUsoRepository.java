package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.ProspeccaoUso;
import br.com.hydroom.rpg.fichacontrolador.model.enums.ProspeccaoUsoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProspeccaoUsoRepository extends JpaRepository<ProspeccaoUso, Long> {

    @Query("SELECT pu FROM ProspeccaoUso pu WHERE pu.fichaProspeccao.ficha.id = :fichaId AND pu.status = :status")
    List<ProspeccaoUso> findByFichaProspeccaoFichaIdAndStatus(
            @Param("fichaId") Long fichaId,
            @Param("status") ProspeccaoUsoStatus status);

    @Query("SELECT pu FROM ProspeccaoUso pu WHERE pu.fichaProspeccao.ficha.id = :fichaId ORDER BY pu.createdAt DESC")
    List<ProspeccaoUso> findByFichaProspeccaoFichaId(@Param("fichaId") Long fichaId);

    @Query("SELECT pu FROM ProspeccaoUso pu WHERE pu.fichaProspeccao.ficha.jogo.id = :jogoId AND pu.status = :status ORDER BY pu.createdAt DESC")
    List<ProspeccaoUso> findByFichaProspeccaoFichaJogoIdAndStatus(
            @Param("jogoId") Long jogoId,
            @Param("status") ProspeccaoUsoStatus status);
}
