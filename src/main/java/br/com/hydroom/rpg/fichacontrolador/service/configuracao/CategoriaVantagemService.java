package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.model.CategoriaVantagem;
import br.com.hydroom.rpg.fichacontrolador.repository.CategoriaVantagemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de categorias de vantagem.
 *
 * <p>Categorias agrupam vantagens (ex: Atributo, Combate, Magia, Social).</p>
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class CategoriaVantagemService extends AbstractConfiguracaoService<CategoriaVantagem, CategoriaVantagemRepository> {

    public CategoriaVantagemService(CategoriaVantagemRepository repository) {
        super(repository, "Categoria de Vantagem");
    }

    @Override
    public List<CategoriaVantagem> listar(Long jogoId) {
        log.debug("Listando categorias de vantagem para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    @Override
    protected void validarAntesCriar(CategoriaVantagem configuracao) {
        if (repository.existsByJogoIdAndNomeIgnoreCase(configuracao.getJogo().getId(), configuracao.getNome())) {
            throw new ConflictException(
                "Já existe uma categoria de vantagem com o nome '" + configuracao.getNome() + "' neste jogo");
        }
    }

    @Override
    protected void validarAntesAtualizar(CategoriaVantagem existente, CategoriaVantagem atualizado) {
        if (!existente.getNome().equalsIgnoreCase(atualizado.getNome())) {
            if (repository.existsByJogoIdAndNomeIgnoreCaseAndIdNot(
                    existente.getJogo().getId(), atualizado.getNome(), existente.getId())) {
                throw new ConflictException(
                    "Já existe uma categoria de vantagem com o nome '" + atualizado.getNome() + "' neste jogo");
            }
        }
    }

    @Override
    protected void atualizarCampos(CategoriaVantagem existente, CategoriaVantagem atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setDescricao(atualizado.getDescricao());
        existente.setCor(atualizado.getCor());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
    }
}
