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

    /** Verifica se o par (vantagem, requisito) já existe (tipo VANTAGEM). */
    boolean existsByVantagemIdAndRequisitoId(Long vantagemId, Long requisitoId);

    /**
     * Retorna vantagens que TÊM a vantagem dada como pré-requisito (tipo VANTAGEM).
     * Usado na detecção de ciclos DFS.
     */
    List<VantagemPreRequisito> findByRequisitoId(Long requisitoId);

    /** Usado para invalidar pré-requisitos ao deletar uma vantagem. */
    List<VantagemPreRequisito> findByVantagemIdOrRequisitoId(Long vantagemId, Long requisitoId);

    /** Verifica se algum pré-req usa esta Raça — bloqueia deleção com 409. */
    boolean existsByRacaId(Long racaId);

    /** Verifica se algum pré-req usa esta Classe — bloqueia deleção com 409. */
    boolean existsByClasseId(Long classeId);

    /** Verifica se algum pré-req usa este Atributo — bloqueia deleção com 409. */
    boolean existsByAtributoId(Long atributoId);

    /** Verifica se algum pré-req usa esta Aptidão — bloqueia deleção com 409. */
    boolean existsByAptidaoId(Long aptidaoId);

    /** Conta quantos pré-requisitos usam esta Raça (para mensagem de erro descritiva). */
    long countByRacaId(Long racaId);

    /** Conta quantos pré-requisitos usam esta Classe (para mensagem de erro descritiva). */
    long countByClasseId(Long classeId);

    /** Conta quantos pré-requisitos usam este Atributo (para mensagem de erro descritiva). */
    long countByAtributoId(Long atributoId);

    /** Conta quantos pré-requisitos usam esta Aptidão (para mensagem de erro descritiva). */
    long countByAptidaoId(Long aptidaoId);
}
