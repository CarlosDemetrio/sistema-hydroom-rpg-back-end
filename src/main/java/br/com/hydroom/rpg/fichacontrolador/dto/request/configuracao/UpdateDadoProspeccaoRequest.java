package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Request DTO para atualizar um dado de prospecção existente.
 *
 * @param nome Nome do dado
 * @param descricao Descrição do dado
 * @param numeroFaces Número de faces do dado
 * @param ordemExibicao Ordem de exibição
 */
public record UpdateDadoProspeccaoRequest(
    @Size(max = 20, message = "Nome deve ter no máximo 20 caracteres")
    String nome,

    @Size(max = 200, message = "Descrição deve ter no máximo 200 caracteres")
    String descricao,

    @Min(value = 1, message = "Número de faces deve ser no mínimo 1")
    @Max(value = 100, message = "Número de faces deve ser no máximo 100")
    Integer numeroFaces,

    Integer ordemExibicao
) {}
