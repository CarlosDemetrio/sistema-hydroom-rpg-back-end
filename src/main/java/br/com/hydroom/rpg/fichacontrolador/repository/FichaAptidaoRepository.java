package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaAptidao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FichaAptidaoRepository extends JpaRepository<FichaAptidao, Long> {

    List<FichaAptidao> findByFichaId(Long fichaId);

    Optional<FichaAptidao> findByFichaIdAndAptidaoConfigId(Long fichaId, Long aptidaoConfigId);

    /**
     * Busca aptidões da ficha ordenadas por ordemExibicao do AptidaoConfig, com JOIN FETCH para evitar N+1.
     * Usar no endpoint GET /fichas/{id}/aptidoes.
     */
    @Query("SELECT fa FROM FichaAptidao fa JOIN FETCH fa.aptidaoConfig ac WHERE fa.ficha.id = :fichaId AND fa.deletedAt IS NULL ORDER BY ac.ordemExibicao ASC")
    List<FichaAptidao> findByFichaIdWithConfigOrdenado(@Param("fichaId") Long fichaId);
}
