package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
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

    /**
     * Cria um DadoProspeccaoConfig, preenchendo {@code ordemExibicao} automaticamente
     * como {@code MAX + 1} se o valor não for informado (null ou 0).
     */
    @Override
    @Transactional
    public DadoProspeccaoConfig criar(DadoProspeccaoConfig configuracao) {
        if (configuracao.getOrdemExibicao() == null || configuracao.getOrdemExibicao() == 0) {
            configuracao.setOrdemExibicao(
                calcularProximaOrdemExibicao(configuracao.getJogo().getId(), "DadoProspeccaoConfig"));
        }
        return super.criar(configuracao);
    }

    public List<DadoProspeccaoConfig> listar(Long jogoId, String nome) {
        if (nome != null && !nome.isBlank()) {
            return repository.findByJogoIdAndNomeContainingIgnoreCaseOrderByOrdemExibicao(jogoId, nome);
        }
        return listar(jogoId);
    }

    @Override
    protected void validarAntesCriar(DadoProspeccaoConfig configuracao) {
        if (repository.existsByJogoIdAndNomeIgnoreCase(configuracao.getJogo().getId(), configuracao.getNome())) {
            throw new ConflictException("Já existe um dado de prospecção com o nome '" + configuracao.getNome() + "' neste jogo");
        }
    }

    @Override
    protected void validarAntesAtualizar(DadoProspeccaoConfig configuracaoExistente, DadoProspeccaoConfig configuracaoAtualizada) {
        if (!configuracaoExistente.getNome().equals(configuracaoAtualizada.getNome())) {
            if (repository.existsByJogoIdAndNomeIgnoreCase(configuracaoExistente.getJogo().getId(), configuracaoAtualizada.getNome())) {
                throw new ConflictException("Já existe um dado de prospecção com o nome '" + configuracaoAtualizada.getNome() + "' neste jogo");
            }
        }
    }

    @Override
    protected void atualizarCampos(DadoProspeccaoConfig existente, DadoProspeccaoConfig atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setDescricao(atualizado.getDescricao());
        existente.setNumeroFaces(atualizado.getNumeroFaces());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
    }
}
