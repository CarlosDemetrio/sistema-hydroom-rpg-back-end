package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.FormulaValidationResult;
import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.service.FormulaEvaluatorService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.SiglaValidationService.TipoSigla;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

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

    @Autowired
    private FormulaEvaluatorService formulaEvaluatorService;

    public AtributoConfiguracaoService(ConfiguracaoAtributoRepository repository) {
        super(repository, "Atributo");
    }

    @Override
    public List<AtributoConfig> listar(Long jogoId) {
        log.debug("Listando atributos para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    public List<AtributoConfig> listar(Long jogoId, String nome) {
        if (nome != null && !nome.isBlank()) {
            return repository.findByJogoIdAndNomeContainingIgnoreCaseOrderByOrdemExibicao(jogoId, nome);
        }
        return listar(jogoId);
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
        validarFormulaImpeto(configuracao.getFormulaImpeto());
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
        validarFormulaImpeto(configuracaoAtualizada.getFormulaImpeto());
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

    private void validarFormulaImpeto(String formula) {
        if (formula == null || formula.isBlank()) return;
        FormulaValidationResult result = formulaEvaluatorService.validarFormula(formula, Set.of("total"));
        if (!result.valid()) {
            String msg = result.erroSintaxe() != null
                ? ValidationMessages.AtributoConfig.FORMULA_IMPETO_SINTAXE_INVALIDA + ": " + result.erroSintaxe()
                : ValidationMessages.AtributoConfig.FORMULA_IMPETO_VARIAVEIS_INVALIDAS
                    .formatted(String.join(", ", result.variaveisInvalidas()));
            throw new ValidationException(msg);
        }
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
