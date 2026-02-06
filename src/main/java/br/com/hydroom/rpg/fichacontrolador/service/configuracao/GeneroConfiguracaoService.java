package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

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
        return repository.findByJogoIdOrderByOrdem(jogoId);
    }

    @Override
    protected void atualizarCampos(GeneroConfig existente, GeneroConfig atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setDescricao(atualizado.getDescricao());
        existente.setOrdem(atualizado.getOrdem());
    }
}
