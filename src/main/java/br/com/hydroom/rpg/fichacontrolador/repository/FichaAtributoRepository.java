package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.FichaAtributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FichaAtributoRepository extends JpaRepository<FichaAtributo, Long> {

    List<FichaAtributo> findByFichaId(Long fichaId);

    Optional<FichaAtributo> findByFichaIdAndAtributoConfigId(Long fichaId, Long atributoConfigId);

    /**
     * Busca atributos da ficha com JOIN FETCH no AtributoConfig para evitar N+1.
     * Usar nos cenários de cálculo e resumo onde atributoConfig.abreviacao/formulaImpeto são acessados.
     */
    @Query("SELECT fa FROM FichaAtributo fa JOIN FETCH fa.atributoConfig WHERE fa.ficha.id = :fichaId AND fa.deletedAt IS NULL")
    List<FichaAtributo> findByFichaIdWithConfig(@Param("fichaId") Long fichaId);

    /**
     * Busca atributos da ficha ordenados por ordemExibicao do AtributoConfig, com JOIN FETCH para evitar N+1.
     * Usar no endpoint GET /fichas/{id}/atributos.
     */
    @Query("SELECT fa FROM FichaAtributo fa JOIN FETCH fa.atributoConfig ac WHERE fa.ficha.id = :fichaId AND fa.deletedAt IS NULL ORDER BY ac.ordemExibicao ASC")
    List<FichaAtributo> findByFichaIdWithConfigOrdenado(@Param("fichaId") Long fichaId);
}
