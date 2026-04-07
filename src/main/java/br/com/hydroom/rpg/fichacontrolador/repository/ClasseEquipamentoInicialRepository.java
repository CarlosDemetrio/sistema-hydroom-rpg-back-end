package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.ClasseEquipamentoInicial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar equipamentos iniciais de classes de personagem.
 */
@Repository
public interface ClasseEquipamentoInicialRepository extends JpaRepository<ClasseEquipamentoInicial, Long> {

    /**
     * Busca todos os equipamentos iniciais de uma classe com item, raridade e tipo carregados.
     */
    @Query("""
        SELECT c FROM ClasseEquipamentoInicial c
        JOIN FETCH c.itemConfig i
        JOIN FETCH i.raridade
        JOIN FETCH i.tipo
        WHERE c.classe.id = :classeId
        ORDER BY c.obrigatorio DESC, c.grupoEscolha NULLS FIRST, i.nome
        """)
    List<ClasseEquipamentoInicial> findByClasseIdWithItems(@Param("classeId") Long classeId);

    List<ClasseEquipamentoInicial> findByClasseId(Long classeId);
}
