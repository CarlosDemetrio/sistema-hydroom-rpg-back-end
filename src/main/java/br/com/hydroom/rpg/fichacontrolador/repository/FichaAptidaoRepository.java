package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaAptidao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FichaAptidaoRepository extends JpaRepository<FichaAptidao, Long> {

    List<FichaAptidao> findByFichaId(Long fichaId);

    Optional<FichaAptidao> findByFichaIdAndAptidaoConfigId(Long fichaId, Long aptidaoConfigId);
}
