package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.NivelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    List<NivelConfig> findByJogoIdAndAtivoTrueOrderByNivel(Long jogoId);

    /**
     * Busca todos os níveis ativos ordenados por nível.
     */
    List<NivelConfig> findByAtivoTrueOrderByNivel();

    /**
     * Busca configuração de um nível específico.
     */
    Optional<NivelConfig> findByNivel(Integer nivel);

    /**
     * Calcula o nível baseado na experiência.
     */
    @Query("SELECT n FROM NivelConfig n WHERE n.xpNecessaria <= :experiencia AND n.ativo = true ORDER BY n.xpNecessaria DESC LIMIT 1")
    Optional<NivelConfig> findNivelPorExperiencia(Long experiencia);

    /**
     * Busca próximo nível após o nível atual.
     */
    @Query("SELECT n FROM NivelConfig n WHERE n.nivel > :nivelAtual AND n.ativo = true ORDER BY n.nivel ASC LIMIT 1")
    Optional<NivelConfig> findProximoNivel(Integer nivelAtual);
}
