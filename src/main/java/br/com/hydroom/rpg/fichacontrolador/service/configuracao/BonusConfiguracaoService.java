package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.model.BonusConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.BonusConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de Bônus.
 *
 * <p>Bônus são modificadores aplicados aos atributos e cálculos do personagem</p>
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class BonusConfiguracaoService extends AbstractConfiguracaoService<BonusConfig, BonusConfigRepository> {

    public BonusConfiguracaoService(BonusConfigRepository repository) {
        super(repository, "Bônus");
    }

    @Override
    public List<BonusConfig> listar(Long jogoId) {
        log.debug("Listando bônus para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    @Override
    protected void validarAntesCriar(BonusConfig configuracao) {
        if (repository.existsByJogoIdAndNomeIgnoreCase(configuracao.getJogo().getId(), configuracao.getNome())) {
            throw new ConflictException("Já existe um bônus com o nome '" + configuracao.getNome() + "' neste jogo");
        }
    }

    @Override
    protected void validarAntesAtualizar(BonusConfig configuracaoExistente, BonusConfig configuracaoAtualizada) {
        if (!configuracaoExistente.getNome().equals(configuracaoAtualizada.getNome())) {
            if (repository.existsByJogoIdAndNomeIgnoreCase(configuracaoExistente.getJogo().getId(), configuracaoAtualizada.getNome())) {
                throw new ConflictException("Já existe um bônus com o nome '" + configuracaoAtualizada.getNome() + "' neste jogo");
            }
        }
    }

    @Override
    protected void atualizarCampos(BonusConfig existente, BonusConfig atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setDescricao(atualizado.getDescricao());
        existente.setFormulaBase(atualizado.getFormulaBase());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
    }
}
