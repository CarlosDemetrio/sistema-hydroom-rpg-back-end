package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaAnotacao;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoAnotacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FichaAnotacaoRepository extends JpaRepository<FichaAnotacao, Long> {

    List<FichaAnotacao> findByFichaIdOrderByCreatedAtDesc(Long fichaId);

    List<FichaAnotacao> findByFichaIdAndTipoAnotacaoOrderByCreatedAtDesc(Long fichaId, TipoAnotacao tipo);

    List<FichaAnotacao> findByFichaIdAndVisivelParaJogadorTrueOrderByCreatedAtDesc(Long fichaId);

    List<FichaAnotacao> findByFichaIdAndPastaPaiIdOrderByCreatedAtDesc(Long fichaId, Long pastaPaiId);

    List<FichaAnotacao> findByPastaPaiId(Long pastaPaiId);
}
