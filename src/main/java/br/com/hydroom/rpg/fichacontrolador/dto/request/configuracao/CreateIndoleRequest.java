package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO para criar uma nova índole.
 *
 * @param jogoId ID do jogo
 * @param nome Nome da índole
 * @param descricao Descrição da índole
 * @param ordemExibicao Ordem de exibição
 */
public record CreateIndoleRequest(
    @NotNull(message = "Jogo é obrigatório")
    Long jogoId,

    @NotBlank(message = "Nome da índole é obrigatório")
    @Size(max = 50, message = "Nome da índole não pode ter mais de 50 caracteres")
    String nome,

    @Size(max = 200, message = "Descrição não pode ter mais de 200 caracteres")
    String descricao,

    Integer ordemExibicao
) {}
