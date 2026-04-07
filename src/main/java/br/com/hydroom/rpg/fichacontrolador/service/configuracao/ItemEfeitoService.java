package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ItemEfeitoRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.AptidaoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.BonusConfig;
import br.com.hydroom.rpg.fichacontrolador.model.ItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.ItemEfeito;
import br.com.hydroom.rpg.fichacontrolador.repository.BonusConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAptidaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ItemEfeitoRepository;
import br.com.hydroom.rpg.fichacontrolador.service.FormulaEvaluatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de efeitos de itens.
 *
 * <p>Valida os campos condicionais obrigatórios conforme o tipoEfeito,
 * a pertinência dos alvos ao mesmo jogo do item e a validade de fórmulas customizadas.</p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemEfeitoService {

    private final ItemEfeitoRepository efeitoRepository;
    private final ItemConfigRepository itemConfigRepository;
    private final ConfiguracaoAtributoRepository atributoRepository;
    private final ConfiguracaoAptidaoRepository aptidaoRepository;
    private final BonusConfigRepository bonusRepository;
    private final FormulaEvaluatorService formulaEvaluatorService;

    /**
     * Lista todos os efeitos de um item.
     */
    public List<ItemEfeito> listarEfeitos(Long itemConfigId) {
        if (!itemConfigRepository.existsById(itemConfigId)) {
            throw new ResourceNotFoundException("ItemConfig", itemConfigId);
        }
        return efeitoRepository.findByItemConfigId(itemConfigId);
    }

    /**
     * Adiciona um novo efeito a um item.
     */
    @Transactional
    public ItemEfeito adicionarEfeito(Long itemConfigId, ItemEfeitoRequest request) {
        ItemConfig item = itemConfigRepository.findById(itemConfigId)
            .orElseThrow(() -> new ResourceNotFoundException("ItemConfig", itemConfigId));

        Long jogoId = item.getJogo().getId();

        ItemEfeito efeito = ItemEfeito.builder()
            .itemConfig(item)
            .tipoEfeito(request.tipoEfeito())
            .valorFixo(request.valorFixo())
            .formula(request.formula())
            .descricaoEfeito(request.descricaoEfeito())
            .build();

        resolverAlvo(efeito, request, jogoId);
        validarCamposObrigatorios(efeito);

        log.debug("Adicionando efeito {} ao item ID: {}", request.tipoEfeito(), itemConfigId);
        return efeitoRepository.save(efeito);
    }

    /**
     * Atualiza um efeito existente de um item.
     */
    @Transactional
    public ItemEfeito atualizarEfeito(Long itemConfigId, Long efeitoId, ItemEfeitoRequest request) {
        ItemEfeito efeito = efeitoRepository.findById(efeitoId)
            .orElseThrow(() -> new ResourceNotFoundException("ItemEfeito", efeitoId));

        if (!efeito.getItemConfig().getId().equals(itemConfigId)) {
            throw new ValidationException("O efeito não pertence ao item informado");
        }

        Long jogoId = efeito.getItemConfig().getJogo().getId();

        efeito.setTipoEfeito(request.tipoEfeito());
        efeito.setValorFixo(request.valorFixo());
        efeito.setFormula(request.formula());
        efeito.setDescricaoEfeito(request.descricaoEfeito());
        efeito.setAtributoAlvo(null);
        efeito.setAptidaoAlvo(null);
        efeito.setBonusAlvo(null);

        resolverAlvo(efeito, request, jogoId);
        validarCamposObrigatorios(efeito);

        log.debug("Atualizando efeito ID: {} do item ID: {}", efeitoId, itemConfigId);
        return efeitoRepository.save(efeito);
    }

    /**
     * Remove fisicamente um efeito de um item (sem soft delete).
     */
    @Transactional
    public void removerEfeito(Long itemConfigId, Long efeitoId) {
        ItemEfeito efeito = efeitoRepository.findById(efeitoId)
            .orElseThrow(() -> new ResourceNotFoundException("ItemEfeito", efeitoId));

        if (!efeito.getItemConfig().getId().equals(itemConfigId)) {
            throw new ValidationException("O efeito não pertence ao item informado");
        }

        efeitoRepository.delete(efeito);
        log.debug("Efeito ID: {} removido do item ID: {}", efeitoId, itemConfigId);
    }

    // ===== HELPERS PRIVADOS =====

    private void resolverAlvo(ItemEfeito efeito, ItemEfeitoRequest request, Long jogoId) {
        switch (request.tipoEfeito()) {
            case BONUS_ATRIBUTO -> {
                if (request.atributoAlvoId() == null) {
                    throw new ValidationException("atributoAlvoId é obrigatório para BONUS_ATRIBUTO");
                }
                AtributoConfig atributo = atributoRepository.findById(request.atributoAlvoId())
                    .orElseThrow(() -> new ResourceNotFoundException("AtributoConfig", request.atributoAlvoId()));
                if (!atributo.getJogo().getId().equals(jogoId)) {
                    throw new ValidationException("AtributoConfig não pertence ao jogo do item");
                }
                efeito.setAtributoAlvo(atributo);
            }
            case BONUS_APTIDAO -> {
                if (request.aptidaoAlvoId() == null) {
                    throw new ValidationException("aptidaoAlvoId é obrigatório para BONUS_APTIDAO");
                }
                AptidaoConfig aptidao = aptidaoRepository.findById(request.aptidaoAlvoId())
                    .orElseThrow(() -> new ResourceNotFoundException("AptidaoConfig", request.aptidaoAlvoId()));
                if (!aptidao.getJogo().getId().equals(jogoId)) {
                    throw new ValidationException("AptidaoConfig não pertence ao jogo do item");
                }
                efeito.setAptidaoAlvo(aptidao);
            }
            case BONUS_DERIVADO -> {
                if (request.bonusAlvoId() == null) {
                    throw new ValidationException("bonusAlvoId é obrigatório para BONUS_DERIVADO");
                }
                BonusConfig bonus = bonusRepository.findById(request.bonusAlvoId())
                    .orElseThrow(() -> new ResourceNotFoundException("BonusConfig", request.bonusAlvoId()));
                if (!bonus.getJogo().getId().equals(jogoId)) {
                    throw new ValidationException("BonusConfig não pertence ao jogo do item");
                }
                efeito.setBonusAlvo(bonus);
            }
            case BONUS_VIDA, BONUS_ESSENCIA, EFEITO_DADO, FORMULA_CUSTOMIZADA -> {
                // Sem FK de alvo — campos obrigatórios validados em validarCamposObrigatorios
            }
        }
    }

    private void validarCamposObrigatorios(ItemEfeito efeito) {
        switch (efeito.getTipoEfeito()) {
            case FORMULA_CUSTOMIZADA -> {
                if (efeito.getFormula() == null || efeito.getFormula().isBlank()) {
                    throw new ValidationException("formula é obrigatória para FORMULA_CUSTOMIZADA");
                }
                if (!formulaEvaluatorService.isValid(efeito.getFormula(), "total", "nivel", "base")) {
                    throw new ValidationException("Fórmula inválida: '" + efeito.getFormula() + "'");
                }
            }
            case EFEITO_DADO -> {
                // valorFixo representa número de posições para avançar no dado
                if (efeito.getValorFixo() == null) {
                    throw new ValidationException("valorFixo é obrigatório para EFEITO_DADO");
                }
            }
            default -> {
                if (efeito.getValorFixo() == null) {
                    throw new ValidationException(
                        "valorFixo é obrigatório para o tipo " + efeito.getTipoEfeito()
                    );
                }
            }
        }
    }
}
