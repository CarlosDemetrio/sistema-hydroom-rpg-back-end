package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO para criar um novo dado de prospecção.
 *
 * @param jogoId ID do jogo
 * @param nome Nome do dado (ex: d4, d6, d8, d10, d12, d20, d100)
 * @param descricao Descrição do dado
 * @param numeroFaces Número de faces do dado (4, 6, 8, 10, 12, 20, 100)
 * @param ordemExibicao Ordem de exibição
 */
public record CreateDadoProspeccaoRequest(
    @NotNull(message = "Jogo é obrigatório")
    Long jogoId,

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 20, message = "Nome deve ter no máximo 20 caracteres")
    String nome,

    @Size(max = 200, message = "Descrição deve ter no máximo 200 caracteres")
    String descricao,

    @NotNull(message = "Número de faces é obrigatório")
    @Min(value = 1, message = "Número de faces deve ser no mínimo 1")
    @Max(value = 100, message = "Número de faces deve ser no máximo 100")
    Integer numeroFaces,

    Integer ordemExibicao
) {}
