package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.IndoleConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.IndoleConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de Índoles.
 *
 * <p>Índoles definem o alinhamento moral do personagem</p>
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class IndoleConfiguracaoService extends AbstractConfiguracaoService<IndoleConfig, IndoleConfigRepository> {

    public IndoleConfiguracaoService(IndoleConfigRepository repository) {
        super(repository, "Índole");
    }

    @Override
    public List<IndoleConfig> listar(Long jogoId) {
        log.debug("Listando índoles para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    @Override
    protected void atualizarCampos(IndoleConfig existente, IndoleConfig atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setDescricao(atualizado.getDescricao());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
    }
}
