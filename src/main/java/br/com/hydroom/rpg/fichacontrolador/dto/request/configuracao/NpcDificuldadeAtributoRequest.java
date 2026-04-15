package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para associar um valor base de atributo a um NpcDificuldadeConfig.
 *
 * @param atributoId ID do AtributoConfig
 * @param valorBase  Valor base pré-definido para o atributo neste nível de dificuldade
 */
public record NpcDificuldadeAtributoRequest(
    @NotNull(message = "ID do atributo é obrigatório")
    Long atributoId,

    @NotNull(message = "Valor base é obrigatório")
    Integer valorBase
) {}
