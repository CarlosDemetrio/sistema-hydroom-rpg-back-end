package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.model.AptidaoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.TipoAptidao;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAptidaoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de Aptidões.
 *
 * <p>Aptidões são habilidades dos personagens (combate, magia, furtividade, etc.)</p>
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class AptidaoConfiguracaoService extends AbstractConfiguracaoService<AptidaoConfig, ConfiguracaoAptidaoRepository> {

    public AptidaoConfiguracaoService(ConfiguracaoAptidaoRepository repository) {
        super(repository, "Aptidão");
    }

    @Override
    public List<AptidaoConfig> listar(Long jogoId) {
        log.debug("Listando aptidões para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    /**
     * Lista aptidões por tipo específico.
     *
     * @param jogoId ID do jogo
     * @param tipoAptidao Tipo de aptidão para filtrar
     * @return Lista de aptidões do tipo especificado
     */
    public List<AptidaoConfig> listarPorTipo(Long jogoId, TipoAptidao tipoAptidao) {
        log.debug("Listando aptidões do tipo '{}' para jogo ID: {}", tipoAptidao.getNome(), jogoId);
        return repository.findByJogoIdAndTipoAptidaoOrderByOrdemExibicao(jogoId, tipoAptidao);
    }

    @Override
    protected void validarAntesCriar(AptidaoConfig configuracao) {
        validateUniqueNome(configuracao.getNome(), configuracao.getJogo().getId());
    }

    @Override
    protected void validarAntesAtualizar(AptidaoConfig configuracaoExistente, AptidaoConfig configuracaoAtualizada) {
        if (!configuracaoExistente.getNome().equals(configuracaoAtualizada.getNome())) {
            validateUniqueNome(configuracaoAtualizada.getNome(), configuracaoExistente.getJogo().getId());
        }
    }

    @Override
    protected void atualizarCampos(AptidaoConfig existente, AptidaoConfig atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setDescricao(atualizado.getDescricao());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
        existente.setTipoAptidao(atualizado.getTipoAptidao());
    }

    private void validateUniqueNome(String nome, Long jogoId) {
        if (repository.existsByJogoIdAndNomeIgnoreCase(jogoId, nome)) {
            throw new ConflictException("Já existe uma aptidão com o nome '" + nome + "' neste jogo");
        }
    }
}
