package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoEfeito;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO para criar um efeito em uma vantagem.
 *
 * <p>Regras de preenchimento dos campos de alvo segundo tipoEfeito:</p>
 * <ul>
 *   <li>BONUS_ATRIBUTO → atributoAlvoId obrigatório</li>
 *   <li>BONUS_APTIDAO → aptidaoAlvoId obrigatório</li>
 *   <li>BONUS_DERIVADO → bonusAlvoId obrigatório</li>
 *   <li>BONUS_VIDA_MEMBRO → membroAlvoId obrigatório</li>
 *   <li>BONUS_VIDA, BONUS_ESSENCIA → nenhum alvo FK necessário</li>
 *   <li>DADO_UP → sem valor numérico</li>
 *   <li>FORMULA_CUSTOMIZADA → formula obrigatório</li>
 * </ul>
 */
public record CriarVantagemEfeitoRequest(

    @NotNull(message = "Tipo de efeito é obrigatório")
    TipoEfeito tipoEfeito,

    Long atributoAlvoId,

    Long aptidaoAlvoId,

    Long bonusAlvoId,

    Long membroAlvoId,

    BigDecimal valorFixo,

    BigDecimal valorPorNivel,

    @Size(max = 200, message = "Fórmula deve ter no máximo 200 caracteres")
    String formula,

    @Size(max = 500, message = "Descrição do efeito deve ter no máximo 500 caracteres")
    String descricaoEfeito

) {}
