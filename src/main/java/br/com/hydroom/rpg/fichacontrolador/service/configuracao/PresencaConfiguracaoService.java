package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.PresencaConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.PresencaConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de Presenças.
 *
 * <p>Presenças definem a aparência/carisma do personagem</p>
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class PresencaConfiguracaoService extends AbstractConfiguracaoService<PresencaConfig, PresencaConfigRepository> {

    public PresencaConfiguracaoService(PresencaConfigRepository repository) {
        super(repository, "Presença");
    }

    @Override
    public List<PresencaConfig> listar(Long jogoId) {
        log.debug("Listando presenças para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    @Override
    protected void atualizarCampos(PresencaConfig existente, PresencaConfig atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setDescricao(atualizado.getDescricao());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
    }
}
