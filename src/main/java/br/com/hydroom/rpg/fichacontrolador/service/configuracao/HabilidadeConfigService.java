package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.model.HabilidadeConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.HabilidadeConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de habilidades.
 *
 * <p>Habilidades podem ser criadas, editadas e deletadas por MESTRE e JOGADOR.</p>
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class HabilidadeConfigService
        extends AbstractConfiguracaoService<HabilidadeConfig, HabilidadeConfigRepository> {

    public HabilidadeConfigService(HabilidadeConfigRepository repository) {
        super(repository, "HabilidadeConfig");
    }

    @Override
    public List<HabilidadeConfig> listar(Long jogoId) {
        log.debug("Listando habilidades para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    /**
     * Cria um HabilidadeConfig, preenchendo {@code ordemExibicao} automaticamente
     * como {@code MAX + 1} se o valor não for informado (null ou 0).
     */
    @Override
    @Transactional
    public HabilidadeConfig criar(HabilidadeConfig configuracao) {
        if (configuracao.getOrdemExibicao() == null || configuracao.getOrdemExibicao() == 0) {
            configuracao.setOrdemExibicao(
                calcularProximaOrdemExibicao(configuracao.getJogo().getId(), "HabilidadeConfig"));
        }
        return super.criar(configuracao);
    }

    @Override
    protected void validarAntesCriar(HabilidadeConfig configuracao) {
        if (repository.existsByJogoIdAndNomeIgnoreCase(
                configuracao.getJogo().getId(), configuracao.getNome())) {
            throw new ConflictException(
                "Já existe uma habilidade com o nome '" + configuracao.getNome() + "' neste jogo");
        }
    }

    @Override
    protected void validarAntesAtualizar(HabilidadeConfig existente, HabilidadeConfig atualizado) {
        if (atualizado.getNome() != null && !existente.getNome().equalsIgnoreCase(atualizado.getNome())) {
            if (repository.existsByJogoIdAndNomeIgnoreCase(
                    existente.getJogo().getId(), atualizado.getNome())) {
                throw new ConflictException(
                    "Já existe uma habilidade com o nome '" + atualizado.getNome() + "' neste jogo");
            }
        }
    }

    @Override
    protected void atualizarCampos(HabilidadeConfig existente, HabilidadeConfig atualizado) {
        if (atualizado.getNome() != null) {
            existente.setNome(atualizado.getNome());
        }
        if (atualizado.getDescricao() != null) {
            existente.setDescricao(atualizado.getDescricao());
        }
        if (atualizado.getDanoEfeito() != null) {
            existente.setDanoEfeito(atualizado.getDanoEfeito());
        }
        if (atualizado.getOrdemExibicao() != null) {
            existente.setOrdemExibicao(atualizado.getOrdemExibicao());
        }
    }
}
