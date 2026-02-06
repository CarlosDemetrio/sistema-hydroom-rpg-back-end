package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.DadoProspeccaoConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.DadoProspeccaoConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de Dados de Prospecção.
 *
 * <p>Dados de prospecção são características iniciais do personagem
 * (dados sorteados na criação da ficha)</p>
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class DadoProspeccaoConfiguracaoService extends AbstractConfiguracaoService<DadoProspeccaoConfig, DadoProspeccaoConfigRepository> {

    public DadoProspeccaoConfiguracaoService(DadoProspeccaoConfigRepository repository) {
        super(repository, "Dado de prospecção");
    }

    @Override
    public List<DadoProspeccaoConfig> listar(Long jogoId) {
        log.debug("Listando dados de prospecção para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    @Override
    protected void atualizarCampos(DadoProspeccaoConfig existente, DadoProspeccaoConfig atualizado) {
        existente.setDescricao(atualizado.getDescricao());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
    }
}
