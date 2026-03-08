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
    List<NivelConfig> findByJogoIdOrderByNivel(Long jogoId);

    /**
     * Busca configuração de um nível específico em um jogo.
     */
    Optional<NivelConfig> findByJogoIdAndNivel(Long jogoId, Integer nivel);

    /**
     * Calcula o nível baseado na experiência para um jogo específico.
     */
    @Query("SELECT n FROM NivelConfig n WHERE n.jogo.id = :jogoId AND n.xpNecessaria <= :experiencia AND n.deletedAt IS NULL ORDER BY n.xpNecessaria DESC LIMIT 1")
    Optional<NivelConfig> findNivelPorExperiencia(Long jogoId, Long experiencia);

    /**
     * Busca próximo nível após o nível atual em um jogo.
     */
    @Query("SELECT n FROM NivelConfig n WHERE n.jogo.id = :jogoId AND n.nivel > :nivelAtual AND n.deletedAt IS NULL ORDER BY n.nivel ASC LIMIT 1")
    Optional<NivelConfig> findProximoNivel(Long jogoId, Integer nivelAtual);

    boolean existsByJogoIdAndNivel(Long jogoId, Integer nivel);
}
