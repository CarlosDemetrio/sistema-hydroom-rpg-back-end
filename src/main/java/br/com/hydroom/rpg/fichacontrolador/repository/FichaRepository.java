package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FichaRepository extends JpaRepository<Ficha, Long> {

    List<Ficha> findByJogoId(Long jogoId);

    List<Ficha> findByJogoIdAndIsNpcFalseOrderByNome(Long jogoId);

    List<Ficha> findByJogoIdAndIsNpcTrue(Long jogoId);

    List<Ficha> findByJogadorId(Long jogadorId);

    List<Ficha> findByJogoIdAndJogadorId(Long jogoId, Long jogadorId);
}
