package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.SiglaValidationService.TipoSigla;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de Atributos.
 *
 * <p>Atributos são características dos personagens (Força, Destreza, Constituição, etc.)</p>
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class AtributoConfiguracaoService extends AbstractConfiguracaoService<AtributoConfig, ConfiguracaoAtributoRepository> {

    @Autowired
    private SiglaValidationService siglaValidationService;

    public AtributoConfiguracaoService(ConfiguracaoAtributoRepository repository) {
        super(repository, "Atributo");
    }

    @Override
    public List<AtributoConfig> listar(Long jogoId) {
        log.debug("Listando atributos para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    @Override
    protected void validarAntesCriar(AtributoConfig configuracao) {
        validateUniqueNome(configuracao.getNome(), configuracao.getJogo().getId());
        siglaValidationService.validarSiglaDisponivel(
            configuracao.getAbreviacao(),
            configuracao.getJogo().getId(),
            null,
            TipoSigla.ATRIBUTO
        );
    }

    @Override
    protected void validarAntesAtualizar(AtributoConfig configuracaoExistente, AtributoConfig configuracaoAtualizada) {
        if (!configuracaoExistente.getNome().equalsIgnoreCase(configuracaoAtualizada.getNome())) {
            validateUniqueNome(configuracaoAtualizada.getNome(), configuracaoExistente.getJogo().getId());
        }
        String abreviacaoNova = configuracaoAtualizada.getAbreviacao();
        if (abreviacaoNova != null && !abreviacaoNova.equalsIgnoreCase(configuracaoExistente.getAbreviacao())) {
            siglaValidationService.validarSiglaDisponivel(
                abreviacaoNova,
                configuracaoExistente.getJogo().getId(),
                configuracaoExistente.getId(),
                TipoSigla.ATRIBUTO
            );
        }
    }

    @Override
    protected void atualizarCampos(AtributoConfig existente, AtributoConfig atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setAbreviacao(atualizado.getAbreviacao());
        existente.setDescricao(atualizado.getDescricao());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
        existente.setFormulaImpeto(atualizado.getFormulaImpeto());
        existente.setDescricaoImpeto(atualizado.getDescricaoImpeto());
        existente.setValorMinimo(atualizado.getValorMinimo());
        existente.setValorMaximo(atualizado.getValorMaximo());
    }

    /**
     * Valida se já existe um atributo com o mesmo nome no jogo.
     *
     * @param nome Nome do atributo
     * @param jogoId ID do jogo
     * @throws ConflictException se nome já existe
     */
    private void validateUniqueNome(String nome, Long jogoId) {
        if (repository.existsByJogoIdAndNomeIgnoreCase(jogoId, nome)) {
            throw new ConflictException("Já existe um atributo com o nome '" + nome + "' neste jogo");
        }
    }
}
