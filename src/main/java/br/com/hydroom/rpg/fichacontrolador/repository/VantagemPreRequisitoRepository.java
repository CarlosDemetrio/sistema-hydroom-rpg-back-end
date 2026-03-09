package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.VantagemPreRequisito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar pré-requisitos de vantagens.
 */
@Repository
public interface VantagemPreRequisitoRepository extends JpaRepository<VantagemPreRequisito, Long> {

    /** Lista todos os pré-requisitos de uma vantagem. */
    List<VantagemPreRequisito> findByVantagemId(Long vantagemId);

    /** Verifica se o par (vantagem, requisito) já existe. */
    boolean existsByVantagemIdAndRequisitoId(Long vantagemId, Long requisitoId);

    /**
     * Retorna vantagens que TÊM a vantagem dada como pré-requisito.
     * Usado na detecção de ciclos DFS.
     */
    List<VantagemPreRequisito> findByRequisitoId(Long requisitoId);

    /** Usado para invalidar pré-requisitos ao deletar uma vantagem. */
    List<VantagemPreRequisito> findByVantagemIdOrRequisitoId(Long vantagemId, Long requisitoId);
}
