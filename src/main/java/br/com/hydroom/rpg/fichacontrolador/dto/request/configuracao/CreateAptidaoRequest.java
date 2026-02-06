package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO para criar uma nova aptidão.
 *
 * @param jogoId ID do jogo ao qual a aptidão pertence
 * @param tipoAptidaoId ID do tipo de aptidão
 * @param nome Nome da aptidão (ex: Acrobacia, Guarda)
 * @param descricao Descrição da aptidão
 * @param ordemExibicao Ordem de exibição na interface
 */
public record CreateAptidaoRequest(
    @NotNull(message = ValidationMessages.CAMPO_OBRIGATORIO)
    Long jogoId,

    @NotNull(message = ValidationMessages.CAMPO_OBRIGATORIO)
    Long tipoAptidaoId,

    @NotBlank(message = ValidationMessages.CAMPO_OBRIGATORIO)
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    String nome,

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    String descricao,

    Integer ordemExibicao
) {}
