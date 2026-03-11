package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.model.GeneroConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.GeneroConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de Gêneros.
 *
 * <p>Gêneros definem a identidade de gênero do personagem</p>
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class GeneroConfiguracaoService extends AbstractConfiguracaoService<GeneroConfig, GeneroConfigRepository> {

    public GeneroConfiguracaoService(GeneroConfigRepository repository) {
        super(repository, "Gênero");
    }

    @Override
    public List<GeneroConfig> listar(Long jogoId) {
        log.debug("Listando gêneros para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    public List<GeneroConfig> listar(Long jogoId, String nome) {
        if (nome != null && !nome.isBlank()) {
            return repository.findByJogoIdAndNomeContainingIgnoreCaseOrderByOrdemExibicao(jogoId, nome);
        }
        return listar(jogoId);
    }

    @Override
    protected void validarAntesCriar(GeneroConfig configuracao) {
        if (repository.existsByJogoIdAndNomeIgnoreCase(configuracao.getJogo().getId(), configuracao.getNome())) {
            throw new ConflictException("Já existe um gênero com o nome '" + configuracao.getNome() + "' neste jogo");
        }
    }

    @Override
    protected void validarAntesAtualizar(GeneroConfig configuracaoExistente, GeneroConfig configuracaoAtualizada) {
        if (!configuracaoExistente.getNome().equals(configuracaoAtualizada.getNome())) {
            if (repository.existsByJogoIdAndNomeIgnoreCase(configuracaoExistente.getJogo().getId(), configuracaoAtualizada.getNome())) {
                throw new ConflictException("Já existe um gênero com o nome '" + configuracaoAtualizada.getNome() + "' neste jogo");
            }
        }
    }

    @Override
    protected void atualizarCampos(GeneroConfig existente, GeneroConfig atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setDescricao(atualizado.getDescricao());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
    }
}
