package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO para criar um novo tipo de aptidão.
 *
 * @param jogoId ID do jogo
 * @param nome Nome do tipo de aptidão (ex: Física, Mental)
 * @param descricao Descrição do tipo de aptidão
 * @param ordemExibicao Ordem de exibição
 */
public record CreateTipoAptidaoRequest(
    @NotNull(message = ValidationMessages.CAMPO_OBRIGATORIO)
    Long jogoId,

    @NotBlank(message = ValidationMessages.CAMPO_OBRIGATORIO)
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    String nome,

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    String descricao,

    Integer ordemExibicao
) {}
