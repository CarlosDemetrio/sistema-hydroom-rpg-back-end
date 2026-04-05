package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.CategoriaVantagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciar categorias de vantagem.
 */
@Repository
public interface CategoriaVantagemRepository extends JpaRepository<CategoriaVantagem, Long> {

    List<CategoriaVantagem> findByJogoIdOrderByOrdemExibicao(Long jogoId);

    boolean existsByJogoIdAndNomeIgnoreCase(Long jogoId, String nome);

    boolean existsByJogoIdAndNomeIgnoreCaseAndIdNot(Long jogoId, String nome, Long id);
}
