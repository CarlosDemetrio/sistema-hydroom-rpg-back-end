package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Size;

/**
 * Request DTO para atualizar uma índole existente.
 *
 * @param nome Nome da índole
 * @param descricao Descrição da índole
 * @param ordem Ordem de exibição
 */
public record UpdateIndoleRequest(
    @Size(max = 50, message = "Nome da índole não pode ter mais de 50 caracteres")
    String nome,

    @Size(max = 200, message = "Descrição não pode ter mais de 200 caracteres")
    String descricao,

    Integer ordem
) {}
