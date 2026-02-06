package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.NivelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciar configurações de níveis.
 */
@Repository
public interface ConfiguracaoNivelRepository extends JpaRepository<NivelConfig, Long> {

    /**
     * Busca todos os níveis ativos de um jogo ordenados por nível.
     */
    @Query("SELECT n FROM NivelConfig n WHERE n.jogo.id = :jogoId AND n.deletedAt IS NULL ORDER BY n.nivel")
    List<NivelConfig> findByJogoIdAndAtivoTrueOrderByNivel(@Param("jogoId") Long jogoId);

    /**
     * Busca todos os níveis ativos ordenados por nível.
     */
    @Query("SELECT n FROM NivelConfig n WHERE n.deletedAt IS NULL ORDER BY n.nivel")
    List<NivelConfig> findByAtivoTrueOrderByNivel();

    /**
     * Busca configuração de um nível específico.
     */
    Optional<NivelConfig> findByNivel(Integer nivel);

    /**
     * Calcula o nível baseado na experiência.
     */
    @Query("SELECT n FROM NivelConfig n WHERE n.xpNecessaria <= :experiencia AND n.deletedAt IS NULL ORDER BY n.xpNecessaria DESC LIMIT 1")
    Optional<NivelConfig> findNivelPorExperiencia(@Param("experiencia") Long experiencia);

    /**
     * Busca próximo nível após o nível atual.
     */
    @Query("SELECT n FROM NivelConfig n WHERE n.nivel > :nivelAtual AND n.deletedAt IS NULL ORDER BY n.nivel ASC LIMIT 1")
    Optional<NivelConfig> findProximoNivel(@Param("nivelAtual") Integer nivelAtual);
}
