package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.ClasseAptidaoBonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar bônus de aptidão de classes de personagem.
 */
@Repository
public interface ClasseAptidaoBonusRepository extends JpaRepository<ClasseAptidaoBonus, Long> {

    List<ClasseAptidaoBonus> findByClasseId(Long classeId);

    /**
     * Busca todos os ClasseAptidaoBonus de uma classe específica com AptidaoConfig carregado via JOIN FETCH.
     * Evita N+1 ao acessar cab.aptidao durante o recálculo da ficha.
     */
    @Query("""
        SELECT cab FROM ClasseAptidaoBonus cab
        JOIN FETCH cab.aptidao
        WHERE cab.classe.id = :classeId
        AND cab.deletedAt IS NULL
        """)
    List<ClasseAptidaoBonus> findByClasseIdWithAptidao(@Param("classeId") Long classeId);

    boolean existsByClasseIdAndAptidaoId(Long classeId, Long aptidaoId);

    void deleteByClasseId(Long classeId);
}
