package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO para criar um novo bônus.
 *
 * @param jogoId ID do jogo
 * @param nome Nome do bônus (ex: B.B.A, B.B.D)
 * @param descricao Descrição do bônus
 * @param formulaBase Fórmula para cálculo do bônus
 * @param ordemExibicao Ordem de exibição
 */
public record CreateBonusRequest(
    @NotNull(message = ValidationMessages.CAMPO_OBRIGATORIO)
    Long jogoId,

    @NotBlank(message = ValidationMessages.CAMPO_OBRIGATORIO)
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    String nome,

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    String descricao,

    @Size(max = 200, message = "Fórmula base deve ter no máximo 200 caracteres")
    String formulaBase,

    Integer ordemExibicao
) {}
