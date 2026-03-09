package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.FormulaValidationResult;
import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.BonusConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.BonusConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.service.FormulaEvaluatorService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.SiglaValidationService.TipoSigla;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Autowired
    private SiglaValidationService siglaValidationService;

    @Autowired
    private FormulaEvaluatorService formulaEvaluatorService;

    @Autowired
    private ConfiguracaoAtributoRepository atributoRepository;

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
        siglaValidationService.validarSiglaDisponivel(
            configuracao.getSigla(),
            configuracao.getJogo().getId(),
            null,
            TipoSigla.BONUS
        );
        validarFormulaBase(configuracao.getFormulaBase(), configuracao.getJogo().getId());
    }

    @Override
    protected void validarAntesAtualizar(BonusConfig configuracaoExistente, BonusConfig configuracaoAtualizada) {
        if (!configuracaoExistente.getNome().equalsIgnoreCase(configuracaoAtualizada.getNome())) {
            if (repository.existsByJogoIdAndNomeIgnoreCase(configuracaoExistente.getJogo().getId(), configuracaoAtualizada.getNome())) {
                throw new ConflictException("Já existe um bônus com o nome '" + configuracaoAtualizada.getNome() + "' neste jogo");
            }
        }
        String siglaNova = configuracaoAtualizada.getSigla();
        if (siglaNova != null && !siglaNova.equalsIgnoreCase(configuracaoExistente.getSigla())) {
            siglaValidationService.validarSiglaDisponivel(
                siglaNova,
                configuracaoExistente.getJogo().getId(),
                configuracaoExistente.getId(),
                TipoSigla.BONUS
            );
        }
        validarFormulaBase(configuracaoAtualizada.getFormulaBase(), configuracaoExistente.getJogo().getId());
    }

    private void validarFormulaBase(String formula, Long jogoId) {
        if (formula == null || formula.isBlank()) return;
        List<String> siglasAtributos = atributoRepository.findAbreviacoesByJogoId(jogoId);
        Set<String> permitidas = new HashSet<>(siglasAtributos);
        permitidas.addAll(Set.of("nivel", "base"));
        FormulaValidationResult result = formulaEvaluatorService.validarFormula(formula, permitidas);
        if (!result.valid()) {
            String msg = result.erroSintaxe() != null
                ? ValidationMessages.BonusConfig.FORMULA_BASE_SINTAXE_INVALIDA + ": " + result.erroSintaxe()
                : ValidationMessages.BonusConfig.FORMULA_BASE_VARIAVEIS_INVALIDAS
                    .formatted(String.join(", ", result.variaveisInvalidas()));
            throw new ValidationException(msg);
        }
    }

    @Override
    protected void atualizarCampos(BonusConfig existente, BonusConfig atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setSigla(atualizado.getSigla());
        existente.setDescricao(atualizado.getDescricao());
        existente.setFormulaBase(atualizado.getFormulaBase());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
    }
}
