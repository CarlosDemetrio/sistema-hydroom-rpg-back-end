package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoItemEfeito;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request para criação ou atualização de ItemEfeito.
 *
 * <p>Campos condicionais obrigatórios conforme tipoEfeito (validados no service):
 * <ul>
 *   <li>BONUS_ATRIBUTO → atributoAlvoId obrigatório</li>
 *   <li>BONUS_APTIDAO → aptidaoAlvoId obrigatório</li>
 *   <li>BONUS_DERIVADO → bonusAlvoId obrigatório</li>
 *   <li>FORMULA_CUSTOMIZADA → formula obrigatória e validada via FormulaEvaluatorService</li>
 *   <li>Demais tipos → valorFixo obrigatório</li>
 * </ul></p>
 */
public record ItemEfeitoRequest(
    @NotNull TipoItemEfeito tipoEfeito,
    Long atributoAlvoId,
    Long aptidaoAlvoId,
    Long bonusAlvoId,
    Integer valorFixo,
    @Size(max = 200) String formula,
    @Size(max = 300) String descricaoEfeito
) {}
