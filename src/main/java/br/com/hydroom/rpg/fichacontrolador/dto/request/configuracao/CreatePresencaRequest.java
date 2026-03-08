package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO para criar uma nova presença.
 *
 * @param jogoId ID do jogo
 * @param nome Nome da presença
 * @param descricao Descrição da presença
 * @param ordemExibicao Ordem de exibição
 */
public record CreatePresencaRequest(
    @NotNull(message = "Jogo é obrigatório")
    Long jogoId,

    @NotBlank(message = "Nome da presença é obrigatório")
    @Size(max = 50, message = "Nome da presença não pode ter mais de 50 caracteres")
    String nome,

    @Size(max = 200, message = "Descrição não pode ter mais de 200 caracteres")
    String descricao,

    Integer ordemExibicao
) {}
