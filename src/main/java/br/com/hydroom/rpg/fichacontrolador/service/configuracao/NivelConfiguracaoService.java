package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.NivelConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoNivelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de Níveis.
 *
 * <p>Níveis definem a progressão do personagem e requisitos de XP</p>
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class NivelConfiguracaoService extends AbstractConfiguracaoService<NivelConfig, ConfiguracaoNivelRepository> {

    public NivelConfiguracaoService(ConfiguracaoNivelRepository repository) {
        super(repository, "Nível");
    }

    @Override
    public List<NivelConfig> listar(Long jogoId) {
        log.debug("Listando níveis para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByNivel(jogoId);
    }

    @Override
    protected void atualizarCampos(NivelConfig existente, NivelConfig atualizado) {
        existente.setNivel(atualizado.getNivel());
        existente.setXpNecessaria(atualizado.getXpNecessaria());
        existente.setPontosAtributo(atualizado.getPontosAtributo());
        existente.setLimitadorAtributo(atualizado.getLimitadorAtributo());
    }
}
