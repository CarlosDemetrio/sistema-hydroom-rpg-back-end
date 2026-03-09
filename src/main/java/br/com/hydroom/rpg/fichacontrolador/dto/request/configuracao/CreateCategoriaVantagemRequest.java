package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO para criar uma nova categoria de vantagem.
 *
 * @param jogoId        ID do jogo
 * @param nome          Nome da categoria
 * @param descricao     Descrição da categoria
 * @param cor           Cor em formato #RRGGBB
 * @param ordemExibicao Ordem de exibição
 */
public record CreateCategoriaVantagemRequest(
    @NotNull(message = "Jogo é obrigatório")
    Long jogoId,

    @NotBlank(message = "Nome da categoria é obrigatório")
    @Size(max = 100, message = "Nome da categoria não pode ter mais de 100 caracteres")
    String nome,

    String descricao,

    @Size(max = 7, message = "Cor deve estar no formato #RRGGBB")
    String cor,

    Integer ordemExibicao
) {}
