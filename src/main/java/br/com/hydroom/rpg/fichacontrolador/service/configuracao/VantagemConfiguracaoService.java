package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de Vantagens.
 *
 * <p>Vantagens são habilidades especiais que personagens podem adquirir</p>
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class VantagemConfiguracaoService extends AbstractConfiguracaoService<VantagemConfig, VantagemConfigRepository> {

    public VantagemConfiguracaoService(VantagemConfigRepository repository) {
        super(repository, "Vantagem");
    }

    @Override
    public List<VantagemConfig> listar(Long jogoId) {
        log.debug("Listando vantagens para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    @Override
    protected void atualizarCampos(VantagemConfig existente, VantagemConfig atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setDescricao(atualizado.getDescricao());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
        existente.setNivelMaximo(atualizado.getNivelMaximo());
        existente.setFormulaCusto(atualizado.getFormulaCusto());
        existente.setDescricaoEfeito(atualizado.getDescricaoEfeito());
    }
}
