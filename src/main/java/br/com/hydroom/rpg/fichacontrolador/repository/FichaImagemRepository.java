package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaImagem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoImagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FichaImagemRepository extends JpaRepository<FichaImagem, Long> {
    List<FichaImagem> findByFichaIdOrderByTipoImagemAscOrdemExibicaoAsc(Long fichaId);
    Optional<FichaImagem> findByFichaIdAndTipoImagem(Long fichaId, TipoImagem tipoImagem);
    long countByFichaId(Long fichaId);
}
