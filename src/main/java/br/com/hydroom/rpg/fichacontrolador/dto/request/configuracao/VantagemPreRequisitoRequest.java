package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoPreRequisito;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO para adicionar um pré-requisito polimórfico a uma vantagem.
 *
 * <p>Regras por tipo:
 * <ul>
 *   <li>VANTAGEM: {@code requisitoId} obrigatório; {@code nivelMinimo} opcional (default 1)</li>
 *   <li>RACA: {@code racaId} obrigatório</li>
 *   <li>CLASSE: {@code classeId} obrigatório</li>
 *   <li>ATRIBUTO: {@code atributoId} e {@code valorMinimo} obrigatórios</li>
 *   <li>NIVEL: {@code valorMinimo} obrigatório</li>
 *   <li>APTIDAO: {@code aptidaoId} e {@code valorMinimo} obrigatórios</li>
 * </ul>
 */
public record VantagemPreRequisitoRequest(

    @NotNull(message = "Tipo do pré-requisito é obrigatório")
    TipoPreRequisito tipo,

    /** [VANTAGEM] ID da vantagem exigida como pré-requisito. */
    Long requisitoId,

    /** [VANTAGEM] Nível mínimo exigido (padrão 1 se null). */
    @Min(value = 1, message = "Nível mínimo deve ser no mínimo 1")
    Integer nivelMinimo,

    /** [RACA] ID da raça exigida. */
    Long racaId,

    /** [CLASSE] ID da classe exigida. */
    Long classeId,

    /** [ATRIBUTO] ID do atributo cujo valorBase deve atingir valorMinimo. */
    Long atributoId,

    /** [APTIDAO] ID da aptidão cujo valorBase deve atingir valorMinimo. */
    Long aptidaoId,

    /** [ATRIBUTO / APTIDAO / NIVEL] Valor mínimo numérico exigido. */
    @Min(value = 1, message = "Valor mínimo deve ser no mínimo 1")
    Integer valorMinimo
) {}
