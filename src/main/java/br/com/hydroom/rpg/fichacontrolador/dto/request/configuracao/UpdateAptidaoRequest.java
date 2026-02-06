package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Size;

/**
 * Request DTO para atualizar uma aptidão existente.
 * Campos null não serão atualizados.
 *
 * @param tipoAptidaoId ID do tipo de aptidão
 * @param nome Nome da aptidão
 * @param descricao Descrição da aptidão
 * @param ordemExibicao Ordem de exibição na interface
 */
public record UpdateAptidaoRequest(
    Long tipoAptidaoId,

    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    String nome,

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    String descricao,

    Integer ordemExibicao
) {}
