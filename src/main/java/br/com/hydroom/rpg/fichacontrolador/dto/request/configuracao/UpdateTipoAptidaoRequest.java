package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Size;

/**
 * Request DTO para atualizar um tipo de aptidão existente.
 *
 * @param nome Nome do tipo de aptidão
 * @param descricao Descrição do tipo de aptidão
 * @param ordemExibicao Ordem de exibição
 */
public record UpdateTipoAptidaoRequest(
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    String nome,

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    String descricao,

    Integer ordemExibicao
) {}
