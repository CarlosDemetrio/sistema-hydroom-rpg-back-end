package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Size;

/**
 * Request DTO para atualizar uma categoria de vantagem existente.
 *
 * @param nome          Nome da categoria
 * @param descricao     Descrição da categoria
 * @param cor           Cor em formato #RRGGBB
 * @param ordemExibicao Ordem de exibição
 */
public record UpdateCategoriaVantagemRequest(
    @Size(max = 100, message = "Nome da categoria não pode ter mais de 100 caracteres")
    String nome,

    String descricao,

    @Size(max = 7, message = "Cor deve estar no formato #RRGGBB")
    String cor,

    Integer ordemExibicao
) {}
