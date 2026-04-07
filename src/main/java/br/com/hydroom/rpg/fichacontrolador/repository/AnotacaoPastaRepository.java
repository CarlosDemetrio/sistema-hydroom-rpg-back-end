package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.AnotacaoPasta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnotacaoPastaRepository extends JpaRepository<AnotacaoPasta, Long> {

    List<AnotacaoPasta> findByFichaIdAndPastaPaiIsNullOrderByOrdemExibicaoAsc(Long fichaId);

    List<AnotacaoPasta> findByPastaPaiIdOrderByOrdemExibicaoAsc(Long pastaPaiId);

    boolean existsByFichaIdAndPastaPaiIsNullAndNome(Long fichaId, String nome);

    boolean existsByFichaIdAndPastaPaiIdAndNome(Long fichaId, Long pastaPaiId, String nome);

    List<AnotacaoPasta> findByFichaIdOrderByOrdemExibicaoAsc(Long fichaId);
}
