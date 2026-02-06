package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO para criar um novo atributo.
 *
 * @param jogoId ID do jogo ao qual o atributo pertence
 * @param nome Nome do atributo (ex: Força, Destreza)
 * @param abreviacao Abreviação do atributo (ex: FOR, DES)
 * @param descricao Descrição do atributo
 * @param formulaImpeto Fórmula para cálculo de ímpeto
 * @param descricaoImpeto Descrição do ímpeto
 * @param valorMinimo Valor mínimo permitido
 * @param valorMaximo Valor máximo permitido
 * @param ordemExibicao Ordem de exibição na interface
 */
public record CreateAtributoRequest(
    @NotNull(message = ValidationMessages.CAMPO_OBRIGATORIO)
    Long jogoId,

    @NotBlank(message = ValidationMessages.CAMPO_OBRIGATORIO)
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    String nome,

    @Size(min = 2, max = 5, message = "Abreviação deve ter entre 2 e 5 caracteres")
    String abreviacao,

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    String descricao,

    @Size(max = 100, message = "Fórmula de ímpeto deve ter no máximo 100 caracteres")
    String formulaImpeto,

    @Size(max = 200, message = "Descrição do ímpeto deve ter no máximo 200 caracteres")
    String descricaoImpeto,

    Integer valorMinimo,

    Integer valorMaximo,

    Integer ordemExibicao
) {}
