package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Size;

/**
 * Request DTO para atualizar um gênero existente.
 *
 * @param nome Nome do gênero
 * @param descricao Descrição do gênero
 * @param ordemExibicao Ordem de exibição
 */
public record UpdateGeneroRequest(
    @Size(max = 50, message = "Nome do gênero não pode ter mais de 50 caracteres")
    String nome,

    @Size(max = 200, message = "Descrição não pode ter mais de 200 caracteres")
    String descricao,

    Integer ordemExibicao
) {}
